import { Swiper, SwiperSlide } from "swiper/react";
import { Pagination, EffectFade, Navigation, Autoplay } from "swiper/modules";
import { Link } from "react-router-dom";
// Swiper styles
import "swiper/css";
import "swiper/css/effect-fade";
import "swiper/css/navigation";
import "swiper/css/autoplay";
import "swiper/css/pagination";
import "swiper/css/scrollbar";
import bannerLists from "../../utils/bannerLists";

const HeroBanner = () => {
  const colors = [
    "bg-banner-color1",
    "bg-banner-color2",
    "bg-banner-color3",
    "bg-banner-color4",
  ];

  return (
    <div className="py-2 rounded-md">
      <Swiper
        grabCursor={true}
        autoplay={{
          delay: 2000,
          disableOnInteraction: false,
        }}
        navigation
        modules={[Pagination, EffectFade, Navigation, Autoplay]}
        pagination={{ clickable: true }}
        scrollbar={{ draggable: true }}
        slidesPerView={1}
      >
        {bannerLists.map((item, i) => (
          <SwiperSlide key={item.id}>
            <div
              className={`carousel-item rounded-md sm:h-[500px] 
                h-96 ${colors[i]}`}
            >
              <div className="flex items-center justify-center">
                <div className="hidden lg:flex justify-center w-1/2 p-8">
                  <div className="text-center">
                    <h3 className="text-3xl text-white font-bold">
                      {item.title}
                    </h3>
                    <h1 className="text-5xl text-white font-bold margin-top">
                      {item.subtitle}
                    </h1>
                    <p className="text-white font-bold mt-4">
                      {item.description}
                    </p>
                    <Link
                      to="/products"
                      className="mt-6 inline-block bg-black text-white py-2 px-4 rounded hover:bg-grey-800"
                    >
                      Shop
                    </Link>
                  </div>
                </div>
                {/* image */}
                <div className="w-full flex justify-center lg:w-1/2 p-4">
                  <img src={item.image} />
                </div>
              </div>
            </div>
          </SwiperSlide>
        ))}
      </Swiper>
    </div>
  );
};

export default HeroBanner;
