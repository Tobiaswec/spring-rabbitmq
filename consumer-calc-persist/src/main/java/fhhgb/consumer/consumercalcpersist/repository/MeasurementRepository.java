package fhhgb.consumer.consumercalcpersist.repository;

import fhhgb.consumer.consumercalcpersist.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement,Integer> {

}
