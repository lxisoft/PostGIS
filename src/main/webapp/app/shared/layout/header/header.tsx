import './header.scss';

import React, { useState } from 'react';

import {
  Collapse,
  Nav,
  Navbar,
  NavbarToggler,
  NavItem,
  NavLink,
  UncontrolledDropdown,
  DropdownToggle,
  DropdownMenu,
  DropdownItem,
} from 'reactstrap';
import LoadingBar from 'react-redux-loading-bar';
import { NavLink as Link } from 'react-router-dom';

import { AccountMenu, AdminMenu, EntitiesMenu } from '../menus';
import { Brand, Home } from './header-components';

export interface IHeaderProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  ribbonEnv: string;
  isInProduction: boolean;
  isOpenAPIEnabled: boolean;
}

const Header = (props: IHeaderProps) => {
  const [menuOpen, setMenuOpen] = useState(false);

  const renderDevRibbon = () =>
    props.isInProduction === false ? (
      <div className="ribbon dev">
        <a href="">Development</a>
      </div>
    ) : null;

  const toggleMenu = () => setMenuOpen(!menuOpen);

  /* jhipster-needle-add-element-to-menu - JHipster will add new menu items here */

  return (
    <div id="app-header">
      {renderDevRibbon()}
      <LoadingBar className="loading-bar" />
      <Navbar data-cy="navbar" dark expand="md" fixed="top" className="jh-navbar">
        <NavbarToggler aria-label="Menu" onClick={toggleMenu} />
        <Brand />
        <Collapse isOpen={menuOpen} navbar>
          <Nav id="header-tabs" className="ms-auto" navbar>
            <Home />
            {props.isAuthenticated && <EntitiesMenu />}
            {/* GeoDelivery spatial menu — PostGIS-powered features */}
            {props.isAuthenticated && (
              <UncontrolledDropdown nav inNavbar id="geo-menu">
                <DropdownToggle nav caret>
                  🗺️ GeoDelivery
                </DropdownToggle>
                <DropdownMenu end>
                  <DropdownItem tag={Link} to="/geo/customers" id="geo-customers-link">
                    👥 Nearby Customers
                  </DropdownItem>
                  <DropdownItem tag={Link} to="/geo/zones" id="geo-zones-link">
                    🏘️ Delivery Zones
                  </DropdownItem>
                  <DropdownItem tag={Link} to="/geo/zone-check" id="geo-zone-check-link">
                    ✅ Zone Checker
                  </DropdownItem>
                </DropdownMenu>
              </UncontrolledDropdown>
            )}
            {props.isAuthenticated && props.isAdmin && <AdminMenu showOpenAPI={props.isOpenAPIEnabled} />}
            <AccountMenu isAuthenticated={props.isAuthenticated} />
          </Nav>
        </Collapse>
      </Navbar>
    </div>
  );
};

export default Header;
