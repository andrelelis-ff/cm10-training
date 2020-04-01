package com.coremedia.ecommerce.studio.rest;

import com.coremedia.cap.content.Content;
import com.coremedia.ecommerce.studio.rest.model.ChildRepresentation;
import com.coremedia.ecommerce.studio.rest.model.Facets;
import com.coremedia.ecommerce.studio.rest.model.Store;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.Map;

/**
 * Category representation for JSON.
 */
public class CategoryRepresentation extends CommerceBeanRepresentation {

  private String name;
  private String shortDescription;
  private String longDescription;
  private String thumbnailUrl;
  private Category parent;
  private List<Category> subCategories;
  private List<Product> products;
  private List<CommerceBean> children;
  private List<Content> pictures;
  private List<Content> downloads;
  private Map<String, ChildRepresentation> childrenByName;
  private Store store;
  private Catalog catalog;
  private String displayName;
  private Facets facets;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public String getShortDescription() {
    return shortDescription;
  }

  public void setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public String getThumbnailUrl(){
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public List<Category> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<Category> subCategories) {
    this.subCategories = subCategories;
  }

  public Category getParent() {
    return parent;
  }

  public void setParent(Category parent) {
    this.parent = parent;
  }

  public List<CommerceBean> getChildren() {
    return children;
  }

  public void setChildren(List<CommerceBean> children) {
    this.children = children;
  }

  public List<Product> getProducts() {
    return products;
  }

  public void setProducts(List<Product> products) {
    this.products = products;
  }

  public Map<String, ChildRepresentation> getChildrenByName() {
    return childrenByName;
  }

  public void setPictures(List<Content> pictures) {
    this.pictures = pictures;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public List<Content> getPictures() {
    return pictures;
  }

  public void setDownloads(List<Content> downloads) {
    this.downloads = downloads;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public List<Content> getDownloads() {
    return downloads;
  }

  public void setChildrenByName(Map<String, ChildRepresentation> childrenByName) {
    this.childrenByName = childrenByName;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public Store getStore() {
    return store;
  }

  public void setStore(Store store) {
    this.store = store;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public Catalog getCatalog() {
    return catalog;
  }

  public void setCatalog(Catalog catalog) {
    this.catalog = catalog;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public String getLongDescription() {
    return longDescription;
  }

  public void setLongDescription(String longDescription) {
    this.longDescription = longDescription;
  }

  public Facets getFacets() {
    return facets;
  }

  public void setFacets(Facets facets) {
    this.facets = facets;
  }
}
