import React from 'react'
import {FormControl,InputLabel,Select,MenuItem} from "@mui/material";
import {useState} from "react";
import { FormHelperText,Button } from '@mui/material';
import Spinners from "../../shared/Spinners";
import {useDispatch,useSelector} from "react-redux";
import toast from "react-hot-toast";
import {updateOrderStatusFromDashboard} from "../../../store/actions/index";

const ORDER_STATUS = ["Pending","Processing","Shipped","Delivered","Cancelled","Accepted"];

const UpdateOrderForm = ({open,setOpen,selectedId,selectedItem,loader,setLoader}) => {

    const [orderStatus,setOrderStatus] = useState(selectedItem?.status || 'Accepted');
    const [error,setError] = useState("");
    const {user} = useSelector((state) => state.auth);
    const isAdmin = user && user?.roles?.includes("ROLE_ADMIN");
    const dispatch = useDispatch();

    const updateOrderStatus = (e)=> {
        e.preventDefault();
        if(!orderStatus) {
            setError("Order status is required.");
            return ;
        }

        dispatch(updateOrderStatusFromDashboard(
            selectedId,
            orderStatus,
            toast,
            setLoader,
            isAdmin
        ));
    };
  return (
    <div className="py-5 relative h-full">
        <form className="space-y-4" onSubmit={updateOrderStatus}>
            <FormControl fullWidth variant="outlined" error={!!error}>
                <InputLabel id="order-status-label">Order Status</InputLabel>
                <Select
                    labelId="order-status-label"
                    label="Order Status"
                    value={orderStatus}
                    onChange={(e) => {
                        setOrderStatus(e.target.value);
                        setError("");
                    }}
                >
                    {
                        ORDER_STATUS.map(
                            (status)=> (
                                <MenuItem key={status} value={status}>
                                    {status}
                                </MenuItem>
                            )
                        )
                    }
                </Select>
                {error && <FromHelperText>{error}</FromHelperText>}
            </FormControl>

            <div className="flex w-full justify-between items-center absolute bottom-14">
                <Button disabled={loader}
                    onClick={()=> setOpen(false)
                    }
                    variant="outlined"
                    className="text-white py-[10px] px-4 text-sm font-medium"
                >
                    Cancel
                </Button>

                <Button
                    className="bg-custom-blue text-white py-[10px] px-4 text-sm font-medium"
                    disabled={loader}
                    type="submit"
                    variant="contained"
                    color="primary"
                >
                   {loader ? (
                    <div className="flex gap-2 items-center">
                        <Spinners/> Loading ...
                    </div>
                    ): ("Update")}
                </Button>
            </div>
        </form>
    </div>
  )
}

export default UpdateOrderForm
/**
 * It converts any value into a boolean.

!value → converts to boolean and negates

!!value → converts to boolean without negation
 */