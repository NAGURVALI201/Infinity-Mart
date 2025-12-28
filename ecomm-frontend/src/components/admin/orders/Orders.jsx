import React from "react";
import { FaShoppingCart } from "react-icons/fa";
import OrderTable from "./OrderTable";
import { useSelector } from "react-redux";
import useOrderFilter from "../../../hooks/useOrderFilter";
import Loader from "../../shared/Loader";
import ErrorPage from "../../shared/ErrorPage";

const Orders = () => {
  useOrderFilter();
  const { adminOrders, pagination } = useSelector((state) => state.orders);
  const { isLoading, errorMessage } = useSelector((state) => state.errors);

  if (isLoading) {
    return <Loader text="Loading ..." />;
  }

  if (errorMessage) {
    return <ErrorPage message={errorMessage} />;
  }

  const emptyOrder = !adminOrders || adminOrders.length === 0;

  return (
    <>
      <div className="pb-6 pt-20">
        {emptyOrder ? (
          <div className="flex flex-col items-center justify-center text-gray-600 py-10">
            <FaShoppingCart size={50} className="mb-3" />
            <h2 className="text-2xl font-semibold">No Orders Placed Yet</h2>
          </div>
        ) : (
          <div>
            <OrderTable adminOrder={adminOrders} pagination={pagination} />
          </div>
        )}
      </div>
    </>
  );
};

export default Orders;
