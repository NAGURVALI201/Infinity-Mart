import { FaAddressBook } from "react-icons/fa";
import Skeleton from "../shared/Skeleton";
import { useState } from "react";
import AddressInfoModal from "./AddressInfoModal";
import AddAddressForm from "./AddAddressForm";
import { useSelector } from "react-redux";
import AddressList from "./AddressList";
import { useDispatch } from "react-redux";
import { deleteUserAddress } from "../../store/actions/index.js";
import DeleteModal from "./DeleteModal";
import toast from "react-hot-toast";

const AddressInfo = ({ address }) => {
  const [openAddressModal, setOpenAddressModal] = useState(false);
  const [openDeleteModal, setOpenDeleteModal] = useState(false);
  const [selectedAddress, setSelectedAddress] = useState("");
  const noAddressExist = !address || address.length === 0;
  const { isLoading, btnLoader } = useSelector((state) => state.errors);

  const addNewAddressHandler = () => {
    setSelectedAddress("");
    setOpenAddressModal(true);
  };

  const dispatch = useDispatch();

  const deleteAddressHandler = () => {
    dispatch(
      deleteUserAddress(toast, selectedAddress?.addressId, setOpenDeleteModal)
    );
  };

  return (
    <div>
      {noAddressExist ? (
        <div className="p-6 rounded-lg max-w-md mx-auto flex flex-col items-center justify-center">
          <FaAddressBook size={50} className="text-gray-500 mb-4" />

          <h1 className="mb-2 text-slate-800 text-center font-semibold text-2xl">
            No Address added Yet
          </h1>
          <p className="mb-2 text-slate-800 text-center ">
            Please add your address to complete purchase
          </p>

          <button
            className="px-4 py-2 bg-blue-600 text-white font-medium rounded hover:bg-blue-700 transition-all"
            onClick={() => addNewAddressHandler()}
          >
            Add Address
          </button>
        </div>
      ) : (
        <div className="relative p-6 rouned-lg max-w-md mx-auto">
          <h1 className="text-slate-800 text-center font-bold text-2xl">
            Select Address
          </h1>

          {isLoading ? (
            <div className="py-4 px-8">
              <Skeleton />
            </div>
          ) : (
            <>
              <div className="space-y-4 pt-6">
                <AddressList
                  addresses={address}
                  setSelectedAddress={setSelectedAddress}
                  setOpenAddressModal={setOpenAddressModal}
                  setOpenDeleteModal={setOpenDeleteModal}
                />
              </div>
              {address.length > 0 && (
                <div>
                  <button
                    className="px-4 py-2 bg-blue-600 text-white font-medium rounded hover:bg-blue-700 transition-all mt-2"
                    onClick={() => addNewAddressHandler()}
                  >
                    Add More
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}
      {/* children is special prop we don't pass it all the comp inside it are it's children. */}
      <AddressInfoModal open={openAddressModal} setOpen={setOpenAddressModal}>
        <AddAddressForm
          address={selectedAddress}
          setOpenAddressModal={setOpenAddressModal}
        />
      </AddressInfoModal>

      <DeleteModal
        open={openDeleteModal}
        loader={btnLoader}
        setOpen={setOpenDeleteModal}
        title="Delete Address"
        onDeleteHandler={deleteAddressHandler}
      />
    </div>
  );
};

export default AddressInfo;
