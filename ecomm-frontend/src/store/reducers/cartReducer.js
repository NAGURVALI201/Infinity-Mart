const initialState = {
  cart: [],
  totalPrice: 0,
  cartId: null,
};

export const cartReducer = (state = initialState, action) => {
  switch (action.type) {
    case "ADD_CART":
      const productToAdd = action.payload;

      const existingProduct = state.cart.find(
        (item) => item.productId === productToAdd.productId
      );

      if (existingProduct) {
        const updatedCart = state.cart.map((item) => {
          if (item.productId === productToAdd.productId) {
            return productToAdd;
          } else {
            return item;
          }
        });

        const updatedTotalPrice = updatedCart?.reduce(
          (acc, curr) =>
            acc + Number(curr?.specialPrice) * Number(curr?.quantity),
          0
        );

        return {
          ...state,
          cart: updatedCart,
          totalPrice: updatedTotalPrice,
        };
      } else {
        const newCart = [...state.cart, productToAdd];

        const updatedTotalPrice = newCart?.reduce(
          (acc, curr) =>
            acc + Number(curr?.specialPrice) * Number(curr?.quantity),
          0
        );

        return {
          ...state,
          cart: newCart,
          totalPrice: updatedTotalPrice,
        };
      }
    case "REMOVE_CART":
      const newCart = state.cart.filter(
        (item) => item.productId !== action.payload.productId
      );

      const updatedTotalPrice = newCart?.reduce(
        (acc, curr) =>
          acc + Number(curr?.specialPrice) * Number(curr?.quantity),
        0
      );

      return {
        ...state,
        cart: newCart,
        totalPrice: updatedTotalPrice,
      };
    case "GET_USER_CART_PRODUCTS":
      return {
        ...state,
        cart: action.payload,
        totalPrice: action.totalPrice,
        cartId: action.cartId,
      };
    case "CLEAR_CART":
      return {
        cart: [],
        totalPrice: 0,
        cartId: null,
      };
    default:
      return state;
  }
};
