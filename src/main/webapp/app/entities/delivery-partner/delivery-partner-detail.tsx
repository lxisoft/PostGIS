import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './delivery-partner.reducer';

export const DeliveryPartnerDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const deliveryPartnerEntity = useAppSelector(state => state.deliveryPartner.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="deliveryPartnerDetailsHeading">Delivery Partner</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{deliveryPartnerEntity.id}</dd>
          <dt>
            <span id="name">Name</span>
          </dt>
          <dd>{deliveryPartnerEntity.name}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{deliveryPartnerEntity.status}</dd>
          <dt>
            <span id="locationString">Location String</span>
          </dt>
          <dd>{deliveryPartnerEntity.locationString}</dd>
        </dl>
        <Button tag={Link} to="/delivery-partner" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/delivery-partner/${deliveryPartnerEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default DeliveryPartnerDetail;
