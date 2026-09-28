# Product copy shell scripts

This repository contains a set of shell scripts that copies products from one CT project to another.

## Prerequisites:
1.   Git bash (latest version)
2.   Nodejs (latest version)
3.   npm (latest version)
4.   yarn (latest version)
5.   babel (latest version)

## General caveats
* The following must be defined in both source and target CT projects. 
  * Types (ex. custom-price) and its attributes
  * Channels
  * Customer groups
  * Product types and its product attributes. The attribute names and labels must match on both projects. If the attribute in question is a list type, then list item key/value must exactly match too.
* Products/discounts must have keys with no spaces or symbol characters except dash and underscore (  - and _ ).
  * Acceptable
    * BASE-PREMIER-202004_satellite
    * EDSPMIGHBOMAX3M
    * 88996503
  * Unacceptable
    * BASE-MAS&#20LATINO_satellite
    * BASE-MAS LATINO_satellite
    * 8899 6503
    * EDSPMIGHBOMAX3M (with space at end)
* The source and target project API keys used in the script must have admin scopes, as it'll need to look at different kinds of CT objects. To add souce and target api keys for another CT project, edit <i>env.sh</i>.
* The slug attribute on a new product in source must not conflict with another product's slug on the target project. Otherwise the new product will fail to be created, giving you the following message:

    <code>[ { code: 'DuplicateField',
    message: 'A duplicate value \'"BOLT-showtime_satellite"\' exists for field \'slug.en\' on \'product:abd2b9a0-1a1c-41b2-81aa-a7a8d6ae9cab\'.',
    duplicateValue: 'BOLT-showtime_satellite',
    conflictingResource:
    { typeId: 'product',
    id: 'abd2b9a0-1a1c-41b2-81aa-a7a8d6ae9cab' }</code>
* The variant keys/skus of a new product in source must not conflict with another product's variants' key/sku on the target project. Otherwise the new product will fail to be created, giving you the following message:

    <code>[ { code: 'DuplicateField',
    message: 'A duplicate value \'"P5286"\' exists for field \'sku\'.',
    duplicateValue: 'P5286',
    field: 'sku' },
    { code: 'DuplicateField',
    message:
    'A duplicate value \'"P5286"\' exists for field \'key\' on one product variant.',
    duplicateValue: 'P5286',
    field: 'key' } ]</code>
* If a single product/discount fails to get created because of missing attribute or a bad product/discount key, then the script will not stop. It'll still process other products/discounts.

## Satellite product copy script
The EPOCH satellite offer/product copy script is responsible for copying the ATG satellite products (with its variants, prices, and attributes) from one CommerceTools project to another.

### Running the script on local using git bash:

* Open git bash and go to the source code root directory.

* Run <i>./satelliteProductCopy.sh [source-project-key] [target-project-key]</i>

    Example:

    ./satelliteProductCopy.sh epoch-staging2 epoch-dev

## Product copy-by-key script
The offer/product copy-by-key script is responsible for copying products by a set of product keys (with its variants, prices, and attributes), specified in a text file, from one CommerceTools project to another.

### Running the script on local using git bash:

* Open git bash and go to the source code root directory.

* Run <i>./productCopy.sh [source-project-key] [target-project-key] [product-key-text-file]</i>

    Example:

    ./productCopy.sh epoch-staging2 epoch-dev products.txt

    * Example products.txt file
        <pre>BOLTON-UP-FAITH-FAMILY_satellite<br/>INSURANCE-UPGRADE-PPP-TO-PPP-WITH-ADH_satellite<br/>BOLTON-SPECIAL-PRICE-CINEMAX_satellite<br/>BASE-CONTRACTMAX-201812<br/>BOLT-3RDSTREAMFREE-202005<br/>BOLT-NBALP-201912</pre>

## CartDiscount copy-by-key script
The cart discount copy-by-key script is responsible for copying cart discounts by a set of cart discount keys (with its attributes), specified in a text file, from one CommerceTools project to another.

### CartDiscount copy script caveats
* Since discounts reference products, the product in the target CT project must also exist before exporting/importing the discount
* The mentioned general caveats in [the general Caveats section](#general-caveats) still apply

### Running the script on local using git bash:

* Open git bash and go to the source code root directory.

* Run <i>./cartDiscountCopy.sh [source-project-key] [target-project-key] [discount-key-text-file]</i>

    Example:

    ./cartDiscountCopy.sh epoch-staging2 epoch-dev discounts.txt

    * Example discounts.txt file
        <pre>88996503<br/>88961493<br/>EDSPEPIX3MON<br/>EDSPMIGHBOMAX3M<br/></pre>
