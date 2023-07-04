package com.stubhub.domain.inventory.listings.v2.newflow.handler;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.adapter.ListingResponseAdapter;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.TaskFailedException;
import com.stubhub.domain.inventory.listings.v2.newflow.task.ParallelTask;
import com.stubhub.domain.inventory.listings.v2.newflow.task.RegularTask;
import com.stubhub.domain.inventory.listings.v2.newflow.task.Task;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;

public abstract class DefaultBusinessFlowHandler implements BusinessFlowHandler {

  private final static Logger log = LoggerFactory.getLogger(DefaultBusinessFlowHandler.class);

  @Autowired
  private InventoryMgr inventoryMgr;

  @Autowired
  private JMSMessageHelper jmsMessageHelper;

  @Autowired
  private ListingResponseAdapter listingResponseAdapter;

  protected ThreadPoolTaskExecutor threadPoolTaskExecutor;

  protected Set<Task<ListingDTO>> taskList = new LinkedHashSet<Task<ListingDTO>>();
  protected List<Future<ListingDTO>> futureList = new ArrayList<Future<ListingDTO>>();

  protected ListingDTO listingDTO;

  public DefaultBusinessFlowHandler(ListingDTO listingDTO) {
    this.listingDTO = listingDTO;
  }

  public abstract ListingResponse execute();

  protected void addRegularTask(RegularTask regularTask) {
    taskList.add(regularTask);
  }

  protected void addRegularTasks(RegularTask... regularTasks) {
    for (RegularTask regularTask : regularTasks) {
      addRegularTask(regularTask);
    }
  }

  // Adding regular task instances
  protected void addRegularTasks(List<Task<ListingDTO>> tasks) {
    for (Task<ListingDTO> task : tasks) {
      addRegularTask((RegularTask) task);
    }
  }

  protected void addParallelTask(ParallelTask parallelTask) {
    taskList.add(parallelTask);
  }

  protected void addParallelTasks(ParallelTask... parallelTasks) {
    for (ParallelTask parallelTask : parallelTasks) {
      addParallelTask(parallelTask);
    }
  }

  @SuppressWarnings("unchecked")
  protected void runTasks() {
    for (Task<ListingDTO> task : taskList) {
      if (task instanceof ParallelTask) {
        runParallelTask((Callable<ListingDTO>) task);
      } else {
        runRegularTask(task);
      }
    }
    taskList.clear();
  }

  private void runParallelTask(Callable<ListingDTO> task) {
    futureList.add(threadPoolTaskExecutor.submit(task));
  }

  private void runRegularTask(Task<ListingDTO> task) {
    // Wait if there are parallel tasks still processing
    if (!futureList.isEmpty()) {
      waitForParallelTasks();

      // All the tasks are completed, clear the futures
      futureList.clear();
    }

    // Execute the regular task
    task.call();
  }

  private void waitForParallelTasks() {
    for (Future<ListingDTO> future : futureList) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        log.error("message=\"Parallel task execution failed\"", e);
        throw new TaskFailedException(ErrorType.SYSTEMERROR, ErrorCodeEnum.systemError);
      }
    }
  }

  // DB
  protected Listing persistData(Listing listing) {
    populateHeaderInfo(listing);
    switch (listingDTO.getListingType().getOperationType()) {
      case CREATE:
        return inventoryMgr.addListing(listing);
      case UPDATE:
        return inventoryMgr.updateListing(listing);
      default:
        // No Update, throw exception
        log.error("message=\"Data persistence not supported\"");
        throw new ListingException(ErrorType.SYSTEMERROR, ErrorCodeEnum.systemError);
    }
  }

  // Messages
  protected void sendMessages(Long listingId) {
    jmsMessageHelper.sendLockInventoryMessage(listingId);
  }

  // Response
  protected ListingResponse generateResponse(Listing listing) {
    return listingResponseAdapter.convertToListingResponse(listing);
  }

  protected void populateHeaderInfo(Listing listing) {
    listing.setIpAddress(listingDTO.getHeaderInfo().getClientIp());
    listing.setLastUpdatedBy(listingDTO.getHeaderInfo().getSubscriber());
  }
}
