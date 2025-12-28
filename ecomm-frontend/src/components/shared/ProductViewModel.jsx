import { Divider } from "@mui/material";
import {
  Button,
  Dialog,
  DialogBackdrop,
  DialogPanel,
  DialogTitle,
} from "@headlessui/react";
import Status from "./Status";
import { MdClose, MdDone } from "react-icons/md";

function ProductViewModel({ open, setOpen, product, isAvailable }) {
  const {
    id,
    productName,
    image,
    description,
    quantity,
    price,
    discount,
    specialPrice,
  } = product;

  const handleClickOpen = () => {
    setOpen(true);
  };

  return (
    <>
      <Dialog
        open={open}
        as="div"
        className="relative z-10"
        onClose={() => setOpen(false)}
      >
        <DialogBackdrop className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" />
        <div className="fixed inset-0 z-10 w-screen overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4">
            <DialogPanel
              transition
              className="relative transfrom overflow-hidden rounded-lg bg-white shadow-xl transition-all md:max-w-[620px] md:min-w-[620px] w-full"
            >
              {image && (
                <div className="flex justify-center aspect-3/2">
                  <img src={image} alt={productName} />
                </div>
              )}

              <div className="px-6 pt-10 pb-2">
                <DialogTitle
                  as="h3"
                  className="lg:text-3xl sm:text-2xl text-xl font-semibold leading-6 text-gray-800"
                >
                  {productName}
                </DialogTitle>

                <div className="space-y-2 text-gray-700 pb-4 pt-2">
                  {/* pricing */}
                  <div className="flex items-center justify-between gap-2">
                    {specialPrice ? (
                      <div className="flex items-center gap-2">
                        <span className="text-gray-400 line-through">
                          ${Number(price).toFixed(2)}
                        </span>

                        <span className="sm:text-xl font-semibold text-slate-700">
                          ${Number(specialPrice).toFixed(2)}
                        </span>
                      </div>
                    ) : (
                      <span className="text-xl font-bold">
                        {"  "}${Number(price).toFixed(2)}
                      </span>
                    )}

                    {isAvailable ? (
                      <Status
                        text="In Stock"
                        icon={MdDone}
                        bg="bg-teal-200"
                        color="text-teal-900"
                      />
                    ) : (
                      <Status
                        text="Out-Of-Stock"
                        icon={MdClose}
                        bg="bg-rose-200"
                        color="text-rose-700"
                      />
                    )}
                  </div>
                </div>
                <Divider />
                <p className="pt-2">{description}</p>
              </div>

              {/* button */}

              <div className="px-6 py-4 flex justify-end gap-4">
                <button
                  onClick={() => setOpen(false)}
                  type="button"
                  className="px-4 py-2 text-sm font-semibold 
                    text-slate-700 hover:text-slate-800 rounded-md
                    border border-slate-300
                    "
                >
                  Close
                </button>
              </div>
            </DialogPanel>
          </div>
        </div>
      </Dialog>
    </>
  );
}

export default ProductViewModel;
