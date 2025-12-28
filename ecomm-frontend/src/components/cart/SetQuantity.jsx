const btnStyles = "border-[1.2px] border-slate-800 px-3 py-1 rounded-sm";

const SetQuantity = ({
  quantity,
  cardCounter,
  handleQtyIncrease,
  handleQtyDecrease,
}) => {
  return (
    <div className="flext gap-8 items-center">
      {cardCounter ? null : <div className="font-semibold">QUANTITY</div>}
      <div className="flex md:flex-row flex-col gap-4 items-center lg:text-[22px] text-sm">
        <button
          onClick={handleQtyDecrease}
          disabled={quantity <= 1}
          className={btnStyles}
        >
          -
        </button>
        <div className="text-red-500">{quantity}</div>
        <button onClick={handleQtyIncrease} className={btnStyles}>
          +
        </button>
      </div>
    </div>
  );
};

export default SetQuantity;
