const initialState = {
  adminOrders: null,
  pagination: {},
};

export const orderReducer = (state = initialState, action) => {
  switch (action.type) {
    case "GET_ADMIN_ORDERS":
      return {
        ...state,
        adminOrders: action.payload,
        pagination: {
          ...state.pagination,
          pageNumber: action.pageNumber,
          pageSize: action.pageSize,
          totalElements: action.totalElements,
          totalPages: action.totalPages,
          lastPage: action.lastPage,
        },
      };
    default:
      return state;
  }
};
