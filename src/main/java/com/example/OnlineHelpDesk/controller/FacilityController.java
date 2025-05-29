package com.example.OnlineHelpDesk.controller;

import com.example.OnlineHelpDesk.model.Facility;
import com.example.OnlineHelpDesk.model.Request;
import com.example.OnlineHelpDesk.model.User;
import com.example.OnlineHelpDesk.repository.FacilityRepository;
import com.example.OnlineHelpDesk.repository.RequestRepository;
import com.example.OnlineHelpDesk.repository.UserRepository;
import com.example.OnlineHelpDesk.vo.FacilityVo;
import com.example.OnlineHelpDesk.vo.MessageResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class FacilityController {

    @Autowired
    FacilityRepository facilityRepository;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/admin/facility")
    public ResponseEntity<MessageResponseVo> createFacility(@RequestBody Facility facility) {
        facilityRepository.save(facility);
        return ResponseEntity.ok(new MessageResponseVo(false, "Facility created"));
    }

    @PutMapping("/admin/facility/{id}")
    public ResponseEntity<?> updateFacility(@PathVariable Integer id, @RequestBody Facility facility) {
        Facility old_facility = facilityRepository.getReferenceById(id);
        old_facility.setName(facility.getName());
        old_facility.setDescription(facility.getDescription());
        old_facility.setLocation(facility.getLocation());
        facilityRepository.save(old_facility);
        return ResponseEntity.ok(new MessageResponseVo(false, "Facility Updated Successfully"));
    }

    @GetMapping("/facility")
    public ResponseEntity<List<FacilityVo>> getAllFacilities() {
        List<Facility> facilities = facilityRepository.findAll();
        List<FacilityVo> facilityVos = new ArrayList<>();
        for (Facility facility : facilities) {
            FacilityVo facilityVo = new FacilityVo();
            facilityVo.setId(facility.getId());
            facilityVo.setName(facility.getName());
            facilityVo.setDescription(facility.getDescription());
            facilityVo.setLocation(facility.getLocation());
            facilityVo.setRequestCount((int) requestRepository.findAllByFacilityId(facility.getId()).stream().count());
            facilityVos.add(facilityVo);
        }
        return ResponseEntity.ok(facilityVos);
    }

    @DeleteMapping("/admin/facility/{id}")
    public ResponseEntity<MessageResponseVo> deleteFacility(@PathVariable Integer id) {
        Facility facility = facilityRepository.getReferenceById(id);
        List<Request> requests = requestRepository.findAllByFacilityId(id);
        requestRepository.deleteAll(requests);
        List<User> users = userRepository.findAllByFacilityId(id);
        userRepository.deleteAll(users);
        facilityRepository.delete(facility);
        return ResponseEntity.ok(new MessageResponseVo(true, "Facility deleted"));
    }
}
