import React from 'react';
import { NavLink as Link } from 'react-router-dom';
import { NavItem, NavLink } from 'reactstrap';

/**
 * GeoSearch navigation menu item — replaces the old Entities menu.
 * Links to the GIS place search page.
 */
export const EntitiesMenu = () => (
  <>
    <NavItem>
      <NavLink tag={Link} to="/search" id="geo-search-link" className="d-flex align-items-center">
        <span>🗺️ Search Places</span>
      </NavLink>
    </NavItem>
    <NavItem>
      <NavLink tag={Link} to="/swiggy" id="swiggy-app-link" className="d-flex align-items-center">
        <span>🍔 Swiggy App</span>
      </NavLink>
    </NavItem>
  </>
);
