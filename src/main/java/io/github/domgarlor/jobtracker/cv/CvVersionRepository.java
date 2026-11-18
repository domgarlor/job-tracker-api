package io.github.domgarlor.jobtracker.cv;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface CvVersionRepository extends JpaRepository<CvVersion, Long> {

	Optional<CvVersion> findBySha256(String sha256);

	List<CvVersion> findByProfileIdOrderByUploadedAtDescIdDesc(Long profileId);

}
