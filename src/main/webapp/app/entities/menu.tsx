import React from 'react';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/delivery-partner">
        Delivery Partner
      </MenuItem>
      <MenuItem icon="asterisk" to="/delivery-zone">
        Delivery Zone
      </MenuItem>
      <MenuItem icon="asterisk" to="/food-order">
        Food Order
      </MenuItem>
      <MenuItem icon="asterisk" to="/restaurant">
        Restaurant
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
