package app.onepass.organizer.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import app.onepass.apis.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organization")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationEntity implements BaseEntity<Organization, OrganizationEntity> {

	@Id
	@GeneratedValue
	private long id;
	@NotNull
	private String name;
	@NotNull
	private boolean is_verified;

	@Override
	public Organization parseAway(OrganizationEntity organizationEntity) {
		return Organization.newBuilder()
				.setId(organizationEntity.getId())
				.setName(organizationEntity.getName())
				.setIsVerified(organizationEntity.is_verified()).build();
	}
}
