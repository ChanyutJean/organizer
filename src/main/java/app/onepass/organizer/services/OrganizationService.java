package app.onepass.organizer.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.onepass.apis.CreateOrganizationRequest;
import app.onepass.apis.GetByIdRequest;
import app.onepass.apis.GetOrganizationByIdResponse;
import app.onepass.apis.GetOrganizationResponse;
import app.onepass.apis.Organization;
import app.onepass.apis.OrganizerServiceGrpc;
import app.onepass.apis.RemoveOrganizationRequest;
import app.onepass.apis.Result;
import app.onepass.apis.UpdateOrganizationRequest;
import app.onepass.apis.UpdateUsersInOrganizationRequest;
import app.onepass.apis.UserRequest;
import app.onepass.organizer.entities.OrganizationEntity;
import app.onepass.organizer.entities.UserOrganizationEntity;
import app.onepass.organizer.messages.OrganizationMessage;
import app.onepass.organizer.repositories.OrganizationRepository;
import app.onepass.organizer.repositories.UserOrganizationRepository;
import app.onepass.organizer.utilities.ServiceUtil;
import io.grpc.stub.StreamObserver;

@Service
public class OrganizationService extends OrganizerServiceGrpc.OrganizerServiceImplBase {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserOrganizationRepository userOrganizationRepository;

    @Override
    @Transactional
    public void createOrganization(CreateOrganizationRequest request, StreamObserver<Result> responseObserver) {

        if (organizationRepository.findById(request.getOrganization().getId()).isPresent()) {

            Result result = ServiceUtil.returnError("An organization with this ID already exists.");

            ServiceUtil.configureResponseObserver(responseObserver, result);

            return;
        }

        OrganizationMessage organizationMessage = new OrganizationMessage(request.getOrganization());

        ServiceUtil.saveEntity(organizationMessage, organizationRepository);

        Result result = ServiceUtil.returnSuccessful("Organization creation successful.");

        ServiceUtil.configureResponseObserver(responseObserver, result);
    }

    @Override
    public void getOrganization(UserRequest request, StreamObserver<GetOrganizationResponse> responseObserver) {

        List<OrganizationEntity> allOrganizationEntities = organizationRepository.findAll();

        List<Organization> allOrganizations = allOrganizationEntities.stream()
                .map(organizationEntity -> organizationEntity.parseEntity().getOrganization())
                .collect(Collectors.toList());

        GetOrganizationResponse getOrganizationResult = GetOrganizationResponse.newBuilder()
                .addAllOrganizations(allOrganizations).build();

        ServiceUtil.configureResponseObserver(responseObserver, getOrganizationResult);

    }

    @Override
    public void getOrganizationById(GetByIdRequest request, StreamObserver<GetOrganizationByIdResponse> responseObserver) {

        OrganizationEntity organizationEntity;

        try {

            organizationEntity = organizationRepository
                    .findById(request.getReadId())
                    .orElseThrow(IllegalArgumentException::new);

        } catch (IllegalArgumentException illegalArgumentException) {

            GetOrganizationByIdResponse result = GetOrganizationByIdResponse
                    .newBuilder()
                    .build();

            ServiceUtil.configureResponseObserver(responseObserver, result);

            return;
        }

        Organization organization = organizationEntity.parseEntity().getOrganization();

        GetOrganizationByIdResponse getOrganizationByIdResult = GetOrganizationByIdResponse
                .newBuilder()
                .setOrganization(organization)
                .build();

        ServiceUtil.configureResponseObserver(responseObserver, getOrganizationByIdResult);

    }

    @Override
    @Transactional
    public void updateOrganization(UpdateOrganizationRequest request, StreamObserver<Result> responseObserver) {

        if (!organizationRepository.findById(request.getOrganization().getId()).isPresent()) {

            Result result = ServiceUtil.returnError("An organization with this ID does not exist.");

            ServiceUtil.configureResponseObserver(responseObserver, result);

            return;
        }

        OrganizationMessage organizationMessage = new OrganizationMessage(request.getOrganization());

        ServiceUtil.saveEntity(organizationMessage, organizationRepository);

        Result result = ServiceUtil.returnSuccessful("Organization update successful.");

        ServiceUtil.configureResponseObserver(responseObserver, result);
    }

    @Override
    @Transactional
    public void removeOrganization(RemoveOrganizationRequest request, StreamObserver<Result> responseObserver) {

        long organizationId = request.getOrganizationId();

        boolean deleteSuccessful = ServiceUtil.deleteEntity(organizationId, organizationRepository);

        if (!deleteSuccessful) {

            Result result = ServiceUtil.returnError("Cannot find organization from given ID.");

            ServiceUtil.configureResponseObserver(responseObserver, result);

            return;

        }

        Result result = ServiceUtil.returnSuccessful("Organization deletion successful.");

        ServiceUtil.configureResponseObserver(responseObserver, result);
    }

    @Override
    @Transactional
    public void addUsersToOrganization(UpdateUsersInOrganizationRequest request, StreamObserver<Result> responseObserver) {

        List<UserOrganizationEntity> userOrganizationEntities = new ArrayList<>();

        for (int index = 0; index < request.getUserIdsCount(); index++) {

            UserOrganizationEntity userOrganizationEntity = UserOrganizationEntity.builder()
                    .userId(request.getUserIds(index))
                    .organizationId(request.getOrganizationId())
                    .build();

            userOrganizationEntities.add(userOrganizationEntity);
        }

        userOrganizationRepository.saveAll(userOrganizationEntities);

        Result result = ServiceUtil.returnSuccessful("Users added to organization.");

        ServiceUtil.configureResponseObserver(responseObserver, result);
    }

    @Override
    @Transactional
    public void removeUsersFromOrganization(UpdateUsersInOrganizationRequest request, StreamObserver<Result> responseObserver) {

        List<Long> userIds = request.getUserIdsList();

        List<UserOrganizationEntity> userOrganizationEntities = userOrganizationRepository
                .findByOrganizationId(request.getOrganizationId());

        List<UserOrganizationEntity> entitiesToDelete = new ArrayList<>();

        //TODO: Optimize!

        for (UserOrganizationEntity userOrganizationEntity : userOrganizationEntities) {

            for (Long userId : userIds) {

                if (userOrganizationEntity.getUserId() == userId) {

                    entitiesToDelete.add(userOrganizationEntity);
                }
            }
        }

        userOrganizationRepository.deleteAll(entitiesToDelete);

        Result result = ServiceUtil.returnSuccessful("Users removed from organization.");

        ServiceUtil.configureResponseObserver(responseObserver, result);

    }
}