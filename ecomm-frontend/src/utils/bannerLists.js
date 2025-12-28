import {
  bannerImageOne,
  bannerImageTwo,
  bannerImageThree,
} from "./appConstants";
import { FaHome, FaShoppingCart } from "react-icons/fa";
import { FaBoxOpen } from "react-icons/fa";
import { FaThList } from "react-icons/fa";
import { FaStore } from "react-icons/fa";

const bannerLists = [
  {
    id: 1,
    image: bannerImageOne,
    title: "Home Comfort",
    subtitle: "Living Room",
    description: "Upgrade your space with cozy and stylish sofas",
  },
  {
    id: 2,
    image: bannerImageTwo,
    title: "Entertainment Hub",
    subtitle: "Smart TV",
    description: "Experience the latest in home entertainment",
  },
  {
    id: 3,
    image: bannerImageThree,
    title: "Playful Picks",
    subtitle: "Kids' Clothing",
    description: "Bright and fun styles for kids, up to 20% off",
  },
];

export const adminNavigation = [
  { name: "Dashboard", href: "/admin", icon: FaHome, current: true },
  {
    name: "Orders",
    href: "/admin/orders",
    icon: FaShoppingCart,
    current: true,
  },
  { name: "Products", href: "/admin/products", icon: FaBoxOpen },
  { name: "Categories", href: "/admin/categories", icon: FaThList },
  { name: "Seller", href: "/admin/sellers", icon: FaStore, current: true },
];

export const sellerNavigation = [
  {
    name: "Orders",
    href: "/admin/orders",
    icon: FaShoppingCart,
    current: true,
  },
  { name: "Products", href: "/admin/products", icon: FaBoxOpen },

];
export default bannerLists;
