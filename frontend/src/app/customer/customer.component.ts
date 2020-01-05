import {Component, OnInit, ViewChild} from '@angular/core';
import {CustomerService} from '../Services/customer.service';
import {GlobalVariables} from '../logic/global-variables';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';

@Component({
  selector: 'app-customer',
  templateUrl: './customer.component.html',
  styleUrls: ['./customer.component.css']
})
export class CustomerComponent implements OnInit {

  private products: Product[] = [];
  private cart: Cart;
  private loading = false;
  private fail = false;

  constructor(private customerService: CustomerService, private router: Router) { }

  ngOnInit() {
    console.log(this.customerService.token);
    if (!this.customerService.token) {
      this.router.navigate(['/customer-sign']);
    } else {
      this.getData();
    }

  }

  getData() {

    this.customerService.getStoreProducts().subscribe(
      res => {
        this.products = res as Product[];
        this.products.forEach(value => {
          value.count = 0;
        });
        console.log(res);
      },
      err => {
        this.showErrorMsg();
      }
    );
    console.log('tera cienzke');
    this.customerService.getCartProducts().subscribe(
      res => {
        this.cart = res as Cart;
        console.log(res);
      },
      err => {
        this.showErrorMsg();
      }
    );
  }

  showErrorMsg() {
    this.loading = false;
    this.fail = true;
  }

  removeProduct(index) {
    this.products[index].count -= 1;
    if(this.products[index].count < 0){
      this.products[index].count = 0;
    }
  }

  addProduct(index) {
    this.products[index].count += 1;
  }

  addToCart(i: number) {
    //this.customerService.addToCart();
  }


}