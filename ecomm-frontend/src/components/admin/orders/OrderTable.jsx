import React, { useState } from "react";
import { DataGrid } from "@mui/x-data-grid";
import { adminOrderTableColumn } from "../../helper/tableColumn.jsx";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import Modal from "../../shared/Modal.jsx";
import UpdateOrderForm from "./UpdateOrderForm";

const OrderTable = ({ adminOrder, pagination }) => {
  const [updateOpenModal, setUpdateOpenModal] = useState(false);
  const [loader, setLoader] = useState(false);
  const [selectedItem, setSelectedItem] = useState("");
  const [currentPage, setCurrentPage] = useState(
    pagination?.pageNumber + 1 || 1
  );

  const [searchParams] = useSearchParams();
  const params = new URLSearchParams(searchParams);
  const pathname = useLocation().pathname;
  const navigate = useNavigate();

  const handlePaginationChange = (paginationModel) => {
    const page = paginationModel.page + 1;
    setCurrentPage(page);
    params.set("page", page.toString());
    navigate(`${pathname}?${params}`);
  };

  const tableRecords = adminOrder?.map((item) => {
    return {
      id: item?.orderId,
      email: item?.email,
      totalAmount: item?.totalAmount,
      status: item?.orderStatus,
      date: item.orderDate,
    };
  });

  const handleEdit = (order) => {
    setSelectedItem(order);
    setUpdateOpenModal(true);
  };

  return (
    <div>
      <h1 className="text-slate-800 text-3xl text-center font-bold pb-6 uppercase">
        All Orders
      </h1>

      <div>
        <DataGrid
          className="w-full"
          rows={tableRecords}
          columns={adminOrderTableColumn(handleEdit)}
          paginationMode="server"
          rowCount={pagination?.totalElements || 0}
          initialState={{
            pagination: {
              paginationModel: {
                pageSize: pagination?.pageSize || 10,
                page: currentPage - 1,
              },
            },
          }}
          onPaginationModelChange={handlePaginationChange}
          pageSizeOptions={[pagination?.pageSize || 10]}
          checkboxSelection
          disableRowSelectionOnClick
          disableColumnResize
          pagination
          paginationOptions={{
            showFirstButton: true,
            showLastButton: true,
            hideNextButton: currentPage === pagination?.totalPages,
          }}
        />
      </div>
      <Modal
        open={updateOpenModal}
        setOpen={setUpdateOpenModal}
        title="Update Order Status"
      >
        <UpdateOrderForm
          open={updateOpenModal}
          setOpen={setUpdateOpenModal}
          loader={loader}
          setLoader={setLoader}
          selectedItem={selectedItem}
          selectedId={selectedItem?.id}
        />
      </Modal>
    </div>
  );
};

export default OrderTable;
