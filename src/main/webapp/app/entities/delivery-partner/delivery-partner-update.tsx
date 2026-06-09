import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { PartnerStatus } from 'app/shared/model/enumerations/partner-status.model';
import { createEntity, getEntity, reset, updateEntity } from './delivery-partner.reducer';

export const DeliveryPartnerUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const deliveryPartnerEntity = useAppSelector(state => state.deliveryPartner.entity);
  const loading = useAppSelector(state => state.deliveryPartner.loading);
  const updating = useAppSelector(state => state.deliveryPartner.updating);
  const updateSuccess = useAppSelector(state => state.deliveryPartner.updateSuccess);
  const partnerStatusValues = Object.keys(PartnerStatus);

  const handleClose = () => {
    navigate('/delivery-partner');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }

    const entity = {
      ...deliveryPartnerEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          status: 'AVAILABLE',
          ...deliveryPartnerEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="geoDeliveryApp.deliveryPartner.home.createOrEditLabel" data-cy="DeliveryPartnerCreateUpdateHeading">
            Create or edit a Delivery Partner
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField name="id" required readOnly id="delivery-partner-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField
                label="Name"
                id="delivery-partner-name"
                name="name"
                data-cy="name"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField label="Status" id="delivery-partner-status" name="status" data-cy="status" type="select">
                {partnerStatusValues.map(partnerStatus => (
                  <option value={partnerStatus} key={partnerStatus}>
                    {partnerStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Location String"
                id="delivery-partner-locationString"
                name="locationString"
                data-cy="locationString"
                type="text"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/delivery-partner" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default DeliveryPartnerUpdate;
