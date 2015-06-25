package com.tracebucket.x1.organization.api.rest.controller;

import com.tracebucket.tron.assembler.AssemblerResolver;
import com.tracebucket.tron.ddd.domain.AggregateId;
import com.tracebucket.tron.ddd.domain.EntityId;
import com.tracebucket.tron.rest.exception.X1Exception;
import com.tracebucket.x1.dictionary.api.domain.jpa.impl.*;
import com.tracebucket.x1.organization.api.domain.impl.jpa.DefaultOrganization;
import com.tracebucket.x1.organization.api.domain.impl.jpa.DefaultOrganizationUnit;
import com.tracebucket.x1.organization.api.domain.impl.jpa.DefaultPosition;
import com.tracebucket.x1.organization.api.domain.impl.jpa.PositionType;
import com.tracebucket.x1.organization.api.rest.resource.*;
import com.tracebucket.x1.organization.api.service.DefaultOrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by sadath on 10-Feb-15.
 */
@RestController
public class OrganizationController implements Organization{

    private static Logger log = LoggerFactory.getLogger(OrganizationController.class);

    @Autowired
    private DefaultOrganizationService organizationService;

    @Autowired
    private AssemblerResolver assemblerResolver;

    @RequestMapping(value = "/organization", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> createOrganization(/*@Valid */@RequestBody DefaultOrganizationResource organizationResource) {
        DefaultOrganization organization = assemblerResolver.resolveEntityAssembler(DefaultOrganization.class, DefaultOrganizationResource.class).toEntity(organizationResource, DefaultOrganization.class);
        try {
            organization = organizationService.save(organization);
        } catch (DataIntegrityViolationException dive) {
            throw new X1Exception("Organization With Name : " + organizationResource.getName() + "Exists", HttpStatus.CONFLICT);
        }
        if(organization != null) {
            organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
            return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.CREATED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/organizationunit", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> addOrganizationUnit(HttpServletRequest request, @PathVariable("organizationUid") String organizationUid, @RequestBody DefaultOrganizationUnitResource organizationUnitResource) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultOrganizationUnit organizationUnit = assemblerResolver.resolveEntityAssembler(DefaultOrganizationUnit.class, DefaultOrganizationUnitResource.class).toEntity(organizationUnitResource, DefaultOrganizationUnit.class);
            DefaultOrganization organization = null;
            try {
                organization = organizationService.addOrganizationUnit(tenantId, organizationUnit, new AggregateId(organizationUid));
            } catch (DataIntegrityViolationException dive) {
                throw new X1Exception("Organization Unit With Name : " + organizationUnitResource.getName() + "Exists", HttpStatus.CONFLICT);
            }
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/organizationunit/update", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> updateOrganizationUnit(HttpServletRequest request, @PathVariable("organizationUid") String organizationUid, @RequestBody DefaultOrganizationUnitResource organizationUnitResource) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultOrganizationUnit organizationUnit = assemblerResolver.resolveEntityAssembler(DefaultOrganizationUnit.class, DefaultOrganizationUnitResource.class).toEntity(organizationUnitResource, DefaultOrganizationUnit.class);
            DefaultOrganization organization = null;
            try {
                organization = organizationService.updateOrganizationUnit(tenantId, organizationUnit, new AggregateId(organizationUid));
            } catch (DataIntegrityViolationException dive) {
                throw new X1Exception("Organization Unit With Name : " + organizationUnitResource.getName() + "Exists", HttpStatus.CONFLICT);
            }
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> getOrganization(HttpServletRequest request, @PathVariable("organizationUid") String organizationUid) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultOrganization organization = organizationService.findOne(tenantId, new AggregateId(organizationUid));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/organizations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<DefaultOrganizationResource>> getOrganizations() {
        List<DefaultOrganization> organizations = organizationService.findAll();
        if(organizations != null && organizations.size() > 0) {
            Iterator<DefaultOrganization> iterator = organizations.iterator();
            while(iterator.hasNext()) {
                DefaultOrganization organization = iterator.next();
                    Set<DefaultOrganizationUnit> organizationUnits = organization.getOrganizationUnits();
                    if (organizationUnits != null) {
                        Iterator<DefaultOrganizationUnit> iterator1 = organizationUnits.iterator();
                        while(iterator1.hasNext()) {
                            DefaultOrganizationUnit organizationUnit = iterator1.next();
                                if (organizationUnit.getParent() != null) {
                                    iterator1.remove();
                                }
                        }
                    }
            }
            return new ResponseEntity<Set<DefaultOrganizationResource>>(assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResources(organizations, DefaultOrganizationResource.class), HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @Override
    @RequestMapping(value = "/organization/{organizationUID}/positions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<DefaultPositionResource>> getPositions(HttpServletRequest request, @PathVariable("organizationUID") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            Set<DefaultPosition> positions = organizationService.getPositions(tenantId, new AggregateId(aggregateId));
            if (positions != null && positions.size() > 0) {
                Set<DefaultPositionResource> positionResources = assemblerResolver.resolveResourceAssembler(DefaultPositionResource.class, DefaultPosition.class).toResources(positions, DefaultPositionResource.class);
                return new ResponseEntity<Set<DefaultPositionResource>>(positionResources, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @RequestMapping(value = "/organization/{organizationUID}/organizationUnits/positions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Set<DefaultPositionResource>>> getOrganizationUnitPositions(HttpServletRequest request, @PathVariable("organizationUID") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            Map<String, Set<DefaultPositionResource>> positionResources = new HashMap<>();
            Map<String, Set<DefaultPosition>> positions = organizationService.getOrganizationUnitPositions(tenantId, new AggregateId(aggregateId));
            if (positions != null && positions.size() > 0) {
                positions.entrySet().stream().forEach(p -> {
                    positionResources.put(p.getKey(), assemblerResolver.resolveResourceAssembler(DefaultPositionResource.class, DefaultPosition.class).toResources(p.getValue(), DefaultPositionResource.class));
                });
                return new ResponseEntity<Map<String, Set<DefaultPositionResource>>>(positionResources, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @RequestMapping(value = "/organization/{organizationUID}/organizationUnits/search/{searchTerm}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<DefaultOrganizationUnitResource>> searchOrganizationUnits(HttpServletRequest request, @PathVariable("organizationUID") String organizationAggregateId, @PathVariable("searchTerm") String searchTerm) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            searchTerm = "(.*)" + searchTerm.toLowerCase() + "(.*)";
            Set<DefaultOrganizationUnit> organizationUnits = organizationService.searchOrganizationUnits(tenantId, new AggregateId(organizationAggregateId), searchTerm);
            if (organizationUnits != null && organizationUnits.size() > 0) {
                Set<DefaultOrganizationUnitResource> organizationUnitResources = assemblerResolver.resolveResourceAssembler(DefaultOrganizationUnitResource.class, DefaultOrganizationUnit.class).toResources(organizationUnits, DefaultOrganizationUnitResource.class);
                return new ResponseEntity<Set<DefaultOrganizationUnitResource>>(organizationUnitResources, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @RequestMapping(value = "/organization/{organizationUID}/positions/search/{searchTerm}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<DefaultPositionResource>> searchPositions(HttpServletRequest request, @PathVariable("organizationUID") String organizationAggregateId, @PathVariable("searchTerm") String searchTerm) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            searchTerm = "(.*)" + searchTerm.toLowerCase() + "(.*)";
            Set<DefaultPosition> positions = organizationService.searchPositions(tenantId, new AggregateId(organizationAggregateId), searchTerm);
            if (positions != null && positions.size() > 0) {
                Set<DefaultPositionResource> positionResources = assemblerResolver.resolveResourceAssembler(DefaultPositionResource.class, DefaultPosition.class).toResources(positions, DefaultPositionResource.class);
                return new ResponseEntity<Set<DefaultPositionResource>>(positionResources, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @RequestMapping(value = "/organization/{organizationUID}/position", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> addPosition(HttpServletRequest request, @RequestBody List<DefaultPositionResource> positionResource, @PathVariable("organizationUID") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            Set<DefaultPosition> position = assemblerResolver.resolveEntityAssembler(DefaultPosition.class, DefaultPositionResource.class).toEntities(positionResource, DefaultPosition.class);
            DefaultOrganization organization = null;
            try {
                organization = organizationService.addPosition(tenantId, new AggregateId(aggregateId), position);
            } catch (DataIntegrityViolationException dive) {
                throw new X1Exception(dive.getRootCause().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @Override
    @RequestMapping(value = "/organization/{organizationUID}/position/{positionUID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultPositionResource> getPosition(HttpServletRequest request, @PathVariable("organizationUID") String aggregateId, @PathVariable("positionUID") String entityId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultPosition position = organizationService.getPosition(tenantId, new AggregateId(aggregateId), new EntityId(entityId));
            if (position != null) {
                DefaultPositionResource positionResource = assemblerResolver.resolveResourceAssembler(DefaultPositionResource.class, DefaultPosition.class).toResource(position, DefaultPositionResource.class);
                return new ResponseEntity<DefaultPositionResource>(positionResource, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @RequestMapping(value = "/organization/position/types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PositionType[]> getPositionTypes(HttpServletRequest request) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            PositionType positionTypes[] = organizationService.getPositionTypes(tenantId);
            if (positionTypes != null) {
                return new ResponseEntity<PositionType[]>(positionTypes, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @RequestMapping(value = "/organization/{organizationUID}/organizationUnit/{organizationUnitUID}/position", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> addPositionToOrganizationUnit(HttpServletRequest request, @PathVariable("organizationUID") AggregateId organizationAggregateId, @PathVariable("organizationUnitUID") EntityId organizationUnitEntityId, @RequestBody List<String> positions) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultOrganization organization = organizationService.addPositionToOrganizationUnit(tenantId, organizationAggregateId, organizationUnitEntityId, new HashSet<>(positions));
            if (organization != null) {
                DefaultOrganizationResource organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @Override
    @RequestMapping(value = "/organization/{organizationUID}/organizationUnits/{organizationUnitUID}/position", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> updatePositionsOfOrganizationUnit(HttpServletRequest request, @PathVariable("organizationUID") AggregateId organizationAggregateId, @PathVariable("organizationUnitUID") EntityId organizationUnitEntityId, @RequestBody List<String> positions) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultOrganization organization = organizationService.updatePositionsOfOrganizationUnit(tenantId, organizationAggregateId, organizationUnitEntityId, new HashSet<>(positions));
            if (organization != null) {
                DefaultOrganizationResource organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @Override
    @RequestMapping(value = "/organization/{organizationUID}/organizationUnit/{organizationUnitUID}/positions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<DefaultPositionResource>> getPositionsOfOrganizationUnit(HttpServletRequest request, @PathVariable("organizationUID") AggregateId organizationAggregateId, @PathVariable("organizationUnitUID") EntityId organizationUnitEntityId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            Set<DefaultPosition> positions = organizationService.getPositionsOfOrganizationUnit(tenantId, organizationAggregateId, organizationUnitEntityId);
            if (positions != null && positions.size() > 0) {
                Set<DefaultPositionResource> positionResources = assemblerResolver.resolveResourceAssembler(DefaultPositionResource.class, DefaultPosition.class).toResources(positions, DefaultPositionResource.class);
                return new ResponseEntity<Set<DefaultPositionResource>>(positionResources, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @RequestMapping(value = "/organization/{organizationUID}/position/{positionUID}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> updatePosition(HttpServletRequest request, @RequestBody DefaultPositionResource positionResource, @PathVariable("organizationUID") String aggregateId, @PathVariable("positionUID") String entityId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultPosition position = assemblerResolver.resolveEntityAssembler(DefaultPosition.class, DefaultPositionResource.class).toEntity(positionResource, DefaultPosition.class);
            DefaultOrganization organization = null;
            try {
                organization = organizationService.updatePosition(tenantId, new AggregateId(aggregateId), position);
            } catch (DataIntegrityViolationException dive) {
                throw new X1Exception(dive.getRootCause().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteOrganization(HttpServletRequest request, @PathVariable("organizationUid") String organizationUid) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            return new ResponseEntity<Boolean>(organizationService.delete(tenantId, new AggregateId(organizationUid)), HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }


    @RequestMapping(value = "/organization/{organizationUid}/basecurrency", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> addBaseCurrency(HttpServletRequest request, @RequestBody DefaultCurrencyResource baseCurrency, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultCurrency defaultCurrency = assemblerResolver.resolveEntityAssembler(DefaultCurrency.class, DefaultCurrencyResource.class).toEntity(baseCurrency, DefaultCurrency.class);
            DefaultOrganization organization = organizationService.addBaseCurrency(tenantId, defaultCurrency, new AggregateId(aggregateId));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/organization/{organizationUid}/timezone", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> addTimezone(HttpServletRequest request, @RequestBody DefaultTimezoneResource timezone, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultTimezone defaultTimezone = assemblerResolver.resolveEntityAssembler(DefaultTimezone.class, DefaultTimezoneResource.class).toEntity(timezone, DefaultTimezone.class);
            DefaultOrganization organization = organizationService.addTimezone(tenantId, defaultTimezone, new AggregateId(aggregateId));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/organizationunit/{parentOrganizationUnitUid}/below", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> addOrganizationUnitBelow(HttpServletRequest request, @RequestBody DefaultOrganizationUnitResource organizationUnit, @PathVariable("parentOrganizationUnitUid") String parentOrganizationUnitUid, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultOrganizationUnit organizationUnit1 = assemblerResolver.resolveEntityAssembler(DefaultOrganizationUnit.class, DefaultOrganizationUnitResource.class).toEntity(organizationUnit, DefaultOrganizationUnit.class);
            DefaultOrganization organization = organizationService.addOrganizationUnitBelow(tenantId, organizationUnit1, new EntityId(parentOrganizationUnitUid), new AggregateId(aggregateId));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

     @RequestMapping(value = "/organization/{organizationUid}/contactperson", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> addContactPerson(HttpServletRequest request, @RequestBody DefaultPersonResource person, @PathVariable("organizationUid") String aggregateId) {
         String tenantId = request.getHeader("tenant_id");
         if(tenantId != null) {
             DefaultPerson defaultPerson = assemblerResolver.resolveEntityAssembler(DefaultPerson.class, DefaultPersonResource.class).toEntity(person, DefaultPerson.class);
             DefaultOrganization organization = organizationService.addContactPerson(tenantId, defaultPerson, new AggregateId(aggregateId));
             DefaultOrganizationResource organizationResource = null;
             if (organization != null) {
                 organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                 return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
             }
         } else {
             return new ResponseEntity(HttpStatus.UNAUTHORIZED);
         }
         return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/organization/{organizationUid}/contactperson/default", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> setDefaultContactPerson(HttpServletRequest request, @RequestBody DefaultPersonResource person, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultPerson defaultPerson = assemblerResolver.resolveEntityAssembler(DefaultPerson.class, DefaultPersonResource.class).toEntity(person, DefaultPerson.class);
            DefaultOrganization organization = organizationService.setDefaultContactPerson(tenantId, defaultPerson, new AggregateId(aggregateId));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/contactnumber", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> addContactNumber(HttpServletRequest request, @RequestBody DefaultPhoneResource phone, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultPhone defaultPhone = assemblerResolver.resolveEntityAssembler(DefaultPhone.class, DefaultPhoneResource.class).toEntity(phone, DefaultPhone.class);
            DefaultOrganization organization = organizationService.addContactNumber(tenantId, defaultPhone, new AggregateId(aggregateId));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/contactnumber/default", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> setDefaultContactNumber(HttpServletRequest request, @RequestBody DefaultPhoneResource phone, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultPhone defaultPhone = assemblerResolver.resolveEntityAssembler(DefaultPhone.class, DefaultPhoneResource.class).toEntity(phone, DefaultPhone.class);
            DefaultOrganization organization = organizationService.setDefaultContactNumber(tenantId, defaultPhone, new AggregateId(aggregateId));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/organization/{organizationUid}/email", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> addEmail(HttpServletRequest request, @RequestBody DefaultEmailResource email, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultEmail defaultEmail = assemblerResolver.resolveEntityAssembler(DefaultEmail.class, DefaultEmailResource.class).toEntity(email, DefaultEmail.class);
            DefaultOrganization organization = organizationService.addEmail(tenantId, defaultEmail, new AggregateId(aggregateId));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/email/default", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> setDefaultEmail(HttpServletRequest request, @RequestBody DefaultEmailResource email, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultEmail defaultEmail = assemblerResolver.resolveEntityAssembler(DefaultEmail.class, DefaultEmailResource.class).toEntity(email, DefaultEmail.class);
            DefaultOrganization organization = organizationService.setDefaultEmail(tenantId, defaultEmail, new AggregateId(aggregateId));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/headoffice", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> setHeadOffice(HttpServletRequest request, @RequestBody DefaultAddressResource address, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultAddress defaultAddress = assemblerResolver.resolveEntityAssembler(DefaultAddress.class, DefaultAddressResource.class).toEntity(address, DefaultAddress.class);
            DefaultOrganization organization = organizationService.setHeadOffice(tenantId, defaultAddress, new AggregateId(aggregateId));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/organization/{organizationUid}/headoffice/to", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> moveHeadOfficeTo(HttpServletRequest request, @RequestBody DefaultAddressResource address, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultAddress defaultAddress = assemblerResolver.resolveEntityAssembler(DefaultAddress.class, DefaultAddressResource.class).toEntity(address, DefaultAddress.class);
            DefaultOrganization organization = organizationService.moveHeadOfficeTo(tenantId, defaultAddress, new AggregateId(aggregateId));
            DefaultOrganizationResource organizationResource = null;
            if (organization != null) {
                organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/headoffice/address", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultAddressResource> getHeadOfficeAddress(HttpServletRequest request, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultAddress address = organizationService.getHeadOfficeAddress(tenantId, new AggregateId(aggregateId));
            if (address != null) {
                DefaultAddressResource addressResource = assemblerResolver.resolveResourceAssembler(DefaultAddressResource.class, DefaultAddress.class).toResource(address, DefaultAddressResource.class);
                return new ResponseEntity<DefaultAddressResource>(addressResource, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/currencies/base", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<DefaultCurrencyResource>> getBaseCurrencies(HttpServletRequest request, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            Set<DefaultCurrency> currencies = organizationService.getBaseCurrencies(tenantId, new AggregateId(aggregateId));
            if (currencies != null && currencies.size() > 0) {
                Set<DefaultCurrencyResource> currencyResources = assemblerResolver.resolveResourceAssembler(DefaultCurrencyResource.class, DefaultCurrency.class).toResources(currencies, DefaultCurrencyResource.class);
                return new ResponseEntity<Set<DefaultCurrencyResource>>(currencyResources, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/organization/{organizationUid}/organizationunits", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<DefaultOrganizationUnitResource>> getOrganizationUnits(HttpServletRequest request, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            Set<DefaultOrganizationUnit> organizationUnits = organizationService.getOrganizationUnits(tenantId, new AggregateId(aggregateId));
            if (organizationUnits != null && organizationUnits.size() > 0) {
                Set<DefaultOrganizationUnitResource> organizationUnitResources = assemblerResolver.resolveResourceAssembler(DefaultOrganizationUnitResource.class, DefaultOrganizationUnit.class).toResources(organizationUnits, DefaultOrganizationUnitResource.class);
                return new ResponseEntity<Set<DefaultOrganizationUnitResource>>(organizationUnitResources, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/contactnumbers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<DefaultPhoneResource>> getContactNumbers(HttpServletRequest request, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            Set<DefaultPhone> phones = organizationService.getContactNumbers(tenantId, new AggregateId(aggregateId));
            if (phones != null && phones.size() > 0) {
                Set<DefaultPhoneResource> phoneResources = assemblerResolver.resolveResourceAssembler(DefaultPhoneResource.class, DefaultPhone.class).toResources(phones, DefaultPhoneResource.class);
                return new ResponseEntity<Set<DefaultPhoneResource>>(phoneResources, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/organization/{organizationUid}/emails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<DefaultEmailResource>> getEmails(HttpServletRequest request, @PathVariable("organizationUid") String aggregateId) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            Set<DefaultEmail> emails = organizationService.getEmails(tenantId, new AggregateId(aggregateId));
            if (emails != null && emails.size() > 0) {
                Set<DefaultEmailResource> emailResources = assemblerResolver.resolveResourceAssembler(DefaultEmailResource.class, DefaultEmail.class).toResources(emails, DefaultEmailResource.class);
                return new ResponseEntity<Set<DefaultEmailResource>>(emailResources, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @Override
    @RequestMapping(value = "/organization/organizationUnits/restructure", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultOrganizationResource> restructureOrganizationUnits(HttpServletRequest request, @RequestBody DefaultOrganizationResource organizationResource) {
        String tenantId = request.getHeader("tenant_id");
        if(tenantId != null) {
            DefaultOrganization organization = null;
            try {
                organization = assemblerResolver.resolveEntityAssembler(DefaultOrganization.class, DefaultOrganizationResource.class).toEntity(organizationResource, DefaultOrganization.class);
                organization = organizationService.restructureOrganizationUnits(tenantId, organization.getAggregateId(), organization.getOrganizationUnits());
                //organization = organizationService.restructureOrganizationUnits2(tenantId, organization.getAggregateId(), organization.getOrganizationUnits());
                if(organization != null) {
                    organizationResource = assemblerResolver.resolveResourceAssembler(DefaultOrganizationResource.class, DefaultOrganization.class).toResource(organization, DefaultOrganizationResource.class);
                    return new ResponseEntity<DefaultOrganizationResource>(organizationResource, HttpStatus.CREATED);
                }
            } catch (DataIntegrityViolationException dive) {
                throw new X1Exception(dive.getRootCause().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
}
