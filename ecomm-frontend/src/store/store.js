import { configureStore } from "@reduxjs/toolkit";
import { productReducer } from "./reducers/ProductReducer";
import { errorReducer } from "./reducers/errorReducer";
import { cartReducer } from "./reducers/cartReducer";
import { authReducer } from "./reducers/authReducer";
import { paymentReducer } from "./reducers/paymentReducer";
import { adminReducer } from "./reducers/adminReducer";
import { orderReducer } from "./reducers/orderReducer";
import { sellerReducer } from "./reducers/sellerReducer";

const user = localStorage.getItem("auth")
  ? JSON.parse(localStorage.getItem("auth"))
  : null;

const cartItems = localStorage.getItem("cartItems")
  ? JSON.parse(localStorage.getItem("cartItems"))
  : [];

const selectedUserCheckoutAddress = localStorage.getItem("CHECKOUT_ADDRESS")
  ? JSON.parse(localStorage.getItem("CHECKOUT_ADDRESS"))
  : [];

const initialState = {
  carts: {
    cart: cartItems,
    totalPrice: 0,
    cartId: null,
  },
  auth: { user: user, selectedUserCheckoutAddress },
};

const store = configureStore({
  reducer: {
    products: productReducer,
    errors: errorReducer,
    carts: cartReducer,
    auth: authReducer,
    payment: paymentReducer,
    admin: adminReducer,
    orders: orderReducer,
    seller: sellerReducer,
  },
  preloadedState: initialState,
});

export default store;
/*
// since we didn't provide the global state in the preloaded state redux derive the state from the each reducers mapping.
{
  products: {
    products: null,
    categories: null,
    pagination: {}
  },
  errors: {
    isLoading: false,
    errorMessage: null
  }
}
*/
