package com.stubhub.domain.inventory.listings.v2.newflow.dto;

import java.util.ArrayList;
import java.util.List;

import com.stubhub.domain.inventory.v2.DTO.Product;

public class ProductInfo {

  private List<Product> addProducts;
  private List<Product> updateProducts;
  private List<Product> deleteProducts;


  public List<Product> getAddProducts() {
    return new ArrayList<>(addProducts);
  }

  public void setAddProducts(List<Product> addProducts) {
    this.addProducts = new ArrayList<>(addProducts);
  }

  public List<Product> getUpdateProducts() {
    return new ArrayList<>(updateProducts);
  }

  public void setUpdateProducts(List<Product> updateProducts) {
    this.updateProducts = new ArrayList<>(updateProducts);
  }

  public List<Product> getDeleteProducts() {
    return new ArrayList<>(deleteProducts);
  }

  public void setDeleteProducts(List<Product> deleteProducts) {
    this.deleteProducts = new ArrayList<>(deleteProducts);
  }

  public boolean isAddProduct() {
    return (addProducts != null && !addProducts.isEmpty());
  }

  public boolean isUpdateProduct() {
    return (updateProducts != null && !updateProducts.isEmpty());
  }

  public boolean isDeleteProduct() {
    return (deleteProducts != null && !deleteProducts.isEmpty());
  }

}
