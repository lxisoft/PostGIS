import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './delivery-request.reducer';

export const DeliveryRequestDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const deliveryRequestEntity = useAppSelector(state => state.deliveryRequest.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="deliveryRequestDetailsHeading">Delivery Request</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{deliveryRequestEntity.id}</dd>
          <dt>
            <span id="requestDate">Request Date</span>
          </dt>
          <dd>
            {deliveryRequestEntity.requestDate ? (
              <TextFormat value={deliveryRequestEntity.requestDate} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{deliveryRequestEntity.status}</dd>
          <dt>Customer</dt>
          <dd>{deliveryRequestEntity.customer ? deliveryRequestEntity.customer.name : ''}</dd>
        </dl>
        <Button tag={Link} to="/delivery-request" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/delivery-request/${deliveryRequestEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default DeliveryRequestDetail;
