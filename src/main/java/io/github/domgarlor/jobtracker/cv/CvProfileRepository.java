package io.github.domgarlor.jobtracker.cv;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface CvProfileRepository extends JpaRepository<CvProfile, Long> {

	boolean existsByNameIgnoreCase(String name);

	List<CvProfile> findAllByOrderByNameAsc();

}
