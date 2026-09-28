import { DiscountsService } from '../discounts';
import { Commercetools } from '../commercetools';

export const CartDiscountExport = ({ apiSourceInfo }) => {
  const service = {};

  const ctApiSource = Commercetools(apiSourceInfo);
  const sourceDiscountService = DiscountsService({ commercetools: ctApiSource, sleepLength: 1500 });

  service.exportByKey = async (discountKeys = []) => {
    let exportedDiscounts = [];

    if (discountKeys.length) {
      console.log(`Exporting cart discounts by keys: ${discountKeys}`);
      exportedDiscounts = await sourceDiscountService.fetchAll({ where: [`(key in ( ${discountKeys.map(k => `"${k}"`).join(',')}))`] });
    }

    return exportedDiscounts;
  };

  return service;
};

export default CartDiscountExport;
