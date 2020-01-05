package com.sbd.databases.service;

import com.sbd.databases.model.Cart;
import com.sbd.databases.model.CartProduct;
import com.sbd.databases.model.Customer;
import com.sbd.databases.model.DTO.*;
import com.sbd.databases.model.ShopOrder;
import com.sbd.databases.repository.CartRepository;
import com.sbd.databases.repository.ShopOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService
{
    private final CartRepository cartRepository;
    private final CustomerService customerService;
    private final ManagerService managerService;
    private final ShopOrderRepository shopOrderRepository;
    private final ProductService productService;
    private final CartProductService cartProductService;

    @Autowired
    public CartService(CartRepository cartRepository, ProductService productService, CustomerService customerService, ManagerService managerService, ShopOrderRepository shopOrderRepository, ProductService productService1, CartProductService cartProductService)
    {
        this.cartRepository = cartRepository;
        this.customerService = customerService;
        this.managerService = managerService;
        this.shopOrderRepository = shopOrderRepository;
        this.productService = productService1;
        this.cartProductService = cartProductService;
    }

    public CartWithProductsDTO getNotConfirmedCartOfCustomer(Customer customer)
    {
        Cart cart = cartRepository.getFirstByCustomerAndConfirmed(customer, false);

        return new CartWithProductsDTO(cart);
    }

    @Transactional
    public CartWithShopOrderDTO checkoutCartOfCustomer(Customer customer, AddressDTO addressDTO) throws ResponseStatusException
    {
        Cart cart = cartRepository.getFirstByCustomerAndConfirmed(customer, false);
        if (!cart.getCartProducts().isEmpty())
        {
            cart.setConfirmed(true);
            cartRepository.save(cart);

            Cart cartNew = new Cart();
            cartNew.setConfirmed(false);
            cartNew.setCustomer(customer);
            cartRepository.save(cartNew);

            ShopOrder shopOrder = new ShopOrder();
            shopOrder.setAddress(addressDTO.toString());
            shopOrder.setManager(managerService.getAvailableManager());
            shopOrder.setCustomer(customer);
            shopOrder.setCart(cart);
            shopOrderRepository.save(shopOrder);

            CartWithProductsDTO cartWithProductsDTO = new CartWithProductsDTO(cart);
            ShopOrderDTO shopOrderDTO = new ShopOrderDTO(shopOrder);

            return new CartWithShopOrderDTO(cartWithProductsDTO, shopOrderDTO);
        }
        else
        {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Cart is empty.");
        }
    }

    public List<CartWithShopOrderDTO> getCartsOfCustomer(Customer customer)
    {
        return cartRepository
                .getAllByCustomer(customer)
                .stream()
                .map(CartWithShopOrderDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartWithProductsDTO addProductToCart(Integer id, AddProductDTO addProductDTO, Customer customer)
    {
        Cart cart = cartRepository.getFirstByCustomerAndConfirmed(customer, false);
        List<CartProduct> cartProducts = cart.getCartProducts();

        for (CartProduct cartProduct : cartProducts)
        {
            if (cartProduct.getProduct().getId().equals(id))
            {
                cartProduct.setCount(addProductDTO.getCount());
                cartProductService.save(cartProduct);

                return new CartWithProductsDTO(cart);
            }
        }

        CartProduct cartProduct = new CartProduct();
        cartProduct.setCount(addProductDTO.getCount());
        cartProduct.setProduct(productService.findById(id));
        cartProduct.setCart(cart);
        cartProductService.save(cartProduct);
        cartProducts.add(cartProduct);
        cart.setCartProducts(cartProducts);

        return new CartWithProductsDTO(cart);
    }

    @Transactional
    public CartWithProductsDTO deleteProductFromCartOfCustomer(Customer customer, Integer productId)
    {
        Cart cart = cartRepository.getFirstByCustomerAndConfirmed(customer, false);
        List<CartProduct> cartProducts = cart.getCartProducts();

        Iterator<CartProduct> iterator = cartProducts.iterator();
        while (iterator.hasNext())
        {
            CartProduct cartProduct = iterator.next();
            if (cartProduct.getProduct().getId().equals(productId))
            {
                cartProductService.delete(cartProduct);
                iterator.remove();
                break;
            }
        }

        cart.setCartProducts(cartProducts);

        return new CartWithProductsDTO(cart);
    }
}
