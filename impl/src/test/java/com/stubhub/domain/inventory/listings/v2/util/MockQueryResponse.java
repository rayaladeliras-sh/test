package com.stubhub.domain.inventory.listings.v2.util;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class MockQueryResponse extends QueryResponse
{
	private String rows ;
	private String seats;
	private String listingId;
	private String eventId;
	private String externalListingId;
	
	public MockQueryResponse ( String rows, String seats )
	{
		this.rows = rows;
		this.seats = seats;
	}
	
	public MockQueryResponse ( String listingId, String eventId, String externalListingId)
	{
		this.listingId = listingId;
		this.eventId= eventId;
		this.externalListingId = externalListingId;
	}
	public SolrDocumentList getResults ()
	{
		SolrDocumentList docList = new SolrDocumentList();
		
		SolrDocument doc = new SolrDocument();
		doc.addField("ROW_DESC", rows);
		doc.addField("SEATS", seats);
		doc.addField("TICKET_ID", listingId);
		doc.addField("EVENT_ID", eventId);
		doc.addField("EXTERNAL_LISTING_ID", externalListingId);
		
		docList.add(doc);
		
		return docList;
	}

}
