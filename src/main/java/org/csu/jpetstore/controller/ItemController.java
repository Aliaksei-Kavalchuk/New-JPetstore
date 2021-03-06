package org.csu.jpetstore.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.csu.jpetstore.bean.Item;
import org.csu.jpetstore.bean.Product;
import org.csu.jpetstore.bean.Supplier;
import org.csu.jpetstore.exception.ApiRequestException;
import org.csu.jpetstore.service.ItemService;
import org.csu.jpetstore.service.ProductService;
import org.csu.jpetstore.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/items")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    /**
     * Add item to a product
     * @param auth
     * @param item
     * @return
     */
    @ApiOperation(value = "Add item to a product" , authorizations = {@Authorization(value = "Bearer")})
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated() and hasRole('SELLER')")
    public Item addItem(@ApiIgnore Authentication auth, @RequestBody Item item) {
        String productId = item.getProductId().toString();
        String supplierId = item.getSupplierId().toString();
        // if product exist
        Product product = productService.selectProductByID(productId);
        if (product == null) {
            throw new ApiRequestException("Product not exist!", HttpStatus.BAD_REQUEST);
        }
        Supplier supplier = supplierService.selectSupplierByID(supplierId);
        // if supplier exist
        if (supplier == null) {
            throw new ApiRequestException("Supplier not exist!", HttpStatus.BAD_REQUEST);
        }
        if (!supplier.getId().equals(product.getSupplierId())) {
            throw new ApiRequestException("Item's supplier ID is not same with product's supplier ID", HttpStatus.BAD_REQUEST);
        }
        if (!supplier.getUserid().toString().equals(auth.getName())) {
            throw new ApiRequestException("You can't add item to this product, you didn't own this.", HttpStatus.FORBIDDEN);
        }
        itemService.insertItem(item);
        return item;
    }

    /**
     * Update item's information
     * Note: this function won't change item's supplierId and productId
     * @param auth
     * @param item
     * @param id
     * @return
     */
    @ApiOperation(value = "Update item's information" , authorizations = {@Authorization(value = "Bearer")})
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    @PreAuthorize("isAuthenticated() and hasRole('SELLER')")
    public Map updateItem(@ApiIgnore Authentication auth, @RequestBody Item item, @PathVariable String id){
        String userid = auth.getName();
        Item item1 = itemService.selectItemByID(id);
        String supplierId = item.getSupplierId().toString();
        //??????supplier?????????supplierid???supplierservice????????????userid????????????????????????????????????item?????????
        // if current user own this item
        if (item1 == null) {
            throw new ApiRequestException("Item not exist!", HttpStatus.BAD_REQUEST);
        }
        if (!supplierService.selectSupplierByID(supplierId).getUserid().toString().equals(userid)) {
            throw new ApiRequestException("You don't have permission to operate.", HttpStatus.BAD_REQUEST);
        }
        itemService.updateItem(item);
        Map data = new HashMap();
        data.put("error", false);
        data.put("message", "Item updated success.");
        data.put("id", id);
        data.put("data", productService.selectProductByID(id));
        return data;
    }


    /**
     * Get all items
     * Filter for supplierId and productId is available
     * @param supplierId
     * @param productId
     * @return
     */
    @ApiOperation(value = "Get all items")
    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public List<Item> selectAllItems(@RequestParam(value = "supplierId", required = false) String supplierId,
                                     @RequestParam(value = "productId", required = false) String productId) {
        if (supplierId != null && productId != null){
            throw new ApiRequestException("You can't pass supplierId and productId both in a request.", HttpStatus.BAD_REQUEST);
        }
        if (supplierId != null) {
            Supplier supplier = supplierService.selectSupplierByID(supplierId);
            if (supplier == null) {
                throw new ApiRequestException("Supplier not exist", HttpStatus.BAD_REQUEST);
            }
            return itemService.selectItemBySupplierId(supplierId);
        } else if (productId != null){
                Product product  = productService.selectProductByID(productId);
                if (product == null){
                    throw new ApiRequestException("Product not exist", HttpStatus.BAD_REQUEST);
                }
                return itemService.selectItemByProductId(productId);
        }
        return itemService.selectAllItems();
    }

    /**
     * Get item by id
     * @param id
     * @return
     */
    @ApiOperation(value = "Query item by id")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public Item getItemByID(@PathVariable String id) {
        Item item = itemService.selectItemByID(id);
        if (item == null) {
            throw new ApiRequestException("Item not exist!", HttpStatus.BAD_REQUEST);
        }
        return item;
    }


    /**deleteItem*/
    @ApiOperation(value = "Delete item", authorizations = {@Authorization(value = "Bearer")})
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public Map deleteItem(@PathVariable String id){
        if (itemService.selectItemByID(id) == null){
            throw new ApiRequestException("Item not exist!", HttpStatus.BAD_REQUEST);
        }
        itemService.deleteItem(id);
        Map data = new HashMap();
        data.put("message", "Item has been deleted.");
        data.put("id", id);
        data.put("error", false);
        return data;
    }



}
