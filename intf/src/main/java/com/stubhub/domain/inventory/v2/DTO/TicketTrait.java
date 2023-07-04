package com.stubhub.domain.inventory.v2.DTO;

import com.stubhub.domain.inventory.v2.enums.Operation;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ticketTrait")
@XmlType(name = "", propOrder = {
        "id",
        "name",
        "type",
        "operation",
        "categoryId",
        "categoryName"
})

public class TicketTrait implements Serializable {
    private static final long serialVersionUID = -6988514715457713204L;

    @XmlElement(name = "id", required = false)
    private String id;

    @XmlElement(name = "name", required = false)
    private String name;

    @XmlElement(name = "type", required = false)
    private String type;

    @XmlElement(name = "categoryId", required = false)
    private Long categoryId;

    @XmlElement(name = "categoryName", required = false)
    private String categoryName;

    @XmlElement(name = "operation", required = false)
    @JsonDeserialize(using = OperationDeserializer.class)
    private Operation operation = Operation.ADD;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TicketTrait{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", categoryId='").append(categoryId).append('\'');
        sb.append(", categoryName='").append(categoryName).append('\'');
        sb.append(", operation=").append(operation);
        sb.append('}');
        return sb.toString();
    }
}
