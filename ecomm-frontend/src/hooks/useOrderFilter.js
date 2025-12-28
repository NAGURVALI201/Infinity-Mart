import { useSearchParams } from "react-router-dom";
import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { getOrdersForDashboard } from "../store/actions/index";
import {useSelector} from "react-redux";

const useOrderFilter = () => {
  const [searchParams] = useSearchParams();
  const dispatch = useDispatch();
  const {user } = useSelector((state) => state.auth);
  const isAdmin = user && user?.roles.includes("ROLE_ADMIN");
  
  useEffect(() => {
    const params = new URLSearchParams();

    const currentPage = searchParams.get("page")
      ? Number(searchParams.get("page"))
      : 1;

    params.set("pageNumber", currentPage - 1);

    const queryString = params.toString();

    dispatch(getOrdersForDashboard(queryString,isAdmin));
  }, [dispatch, searchParams]);
};

export default useOrderFilter;
