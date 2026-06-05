import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './delivery-zone.reducer';

export const DeliveryZoneDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const deliveryZoneEntity = useAppSelector(state => state.deliveryZone.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="deliveryZoneDetailsHeading">Delivery Zone</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{deliveryZoneEntity.id}</dd>
          <dt>
            <span id="name">Name</span>
          </dt>
          <dd>{deliveryZoneEntity.name}</dd>
          <dt>
            <span id="description">Description</span>
          </dt>
          <dd>{deliveryZoneEntity.description}</dd>
          <dt>
            <span id="active">Active</span>
          </dt>
          <dd>{deliveryZoneEntity.active ? 'true' : 'false'}</dd>
        </dl>
        <Button tag={Link} to="/delivery-zone" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/delivery-zone/${deliveryZoneEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default DeliveryZoneDetail;
