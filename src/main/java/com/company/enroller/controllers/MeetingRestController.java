package com.company.enroller.controllers;

import com.company.enroller.model.Participant;
import com.company.enroller.persistence.ParticipantService;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.company.enroller.model.Participant;
import com.company.enroller.persistence.ParticipantService;
import com.company.enroller.model.Meeting;
import com.company.enroller.persistence.MeetingService;

@RestController
@RequestMapping("/meetings")

public class MeetingRestController {

    @Autowired
    MeetingService meetingService;

    @Autowired
    ParticipantService participantService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getMeetingsWithParameters(@RequestParam(value = "title", defaultValue = "", required = false) String title,
                                                       @RequestParam(value = "sortMode", defaultValue = "", required = false) String sortMode,
                                                       @RequestParam(value = "description", defaultValue = "", required = false) String description,
                                                       @RequestParam(value = "participant", defaultValue = "", required = false) String participantLogin) {
        Participant participant = participantService.findByLogin(participantLogin);
        if (participant == null && participantLogin.length() > 0) {
            return new ResponseEntity<String>("A participant with login " + participantLogin + " does not exist.",
                    HttpStatus.NOT_FOUND);
        }
        Collection<Meeting> meetings = meetingService.findMeetings(title, description, participant, sortMode);
        return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getMeeting(@PathVariable("id") long id) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> addMeeting(@RequestBody Meeting meeting) {
        if (meetingService.findById(meeting.getId()) != null) {
            return new ResponseEntity<String>(
                    "Unable to create. A meeting with id " + meeting.getId() + " already exist.",
                    HttpStatus.CONFLICT);
        }
        if (meetingService.alreadyExist(meeting)) {
            return new ResponseEntity<String>(
                    "Unable to create. A meeting with this title and date already exists " +
                            meeting.getTitle() + " " + meeting.getDate(),
                    HttpStatus.CONFLICT);
        }
        meetingService.add(meeting);
        return new ResponseEntity<Meeting>(meeting, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
        meetingService.delete(meeting);
        return new ResponseEntity<Meeting>(HttpStatus.OK);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody Meeting updatedMeeting) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        updatedMeeting.setId(id);
        meetingService.update(updatedMeeting);
        return new ResponseEntity<Meeting>(updatedMeeting, HttpStatus.OK);
    }


    @RequestMapping(value = "/{id}/participants", method = RequestMethod.GET)
    public ResponseEntity<?> getAllParticipantsFromAMeeting(@PathVariable("id") long id) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<Collection<Participant>>(meeting.getParticipants(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{id}/participants", method = RequestMethod.POST)
    public ResponseEntity<?> addParticipant(@PathVariable("id") long id, @RequestBody Participant participantRequested) {
        Meeting meeting = meetingService.findById(id);
        Participant participant = participantService.findByLogin(participantRequested.getLogin());
        if (meeting == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        if (participant == null) {
            return new ResponseEntity<String>("A participant with login " + participantRequested.getLogin() + " does not exist.",
                    HttpStatus.NOT_FOUND);
        }
        if (meeting.getParticipants().contains(participant)) {
            return new ResponseEntity<String>(
                    "Unable to add. This participant " + participantRequested.getLogin() + " has already been added to meeting " +
                            meeting.getId(),
                    HttpStatus.CONFLICT);
        }
        meetingService.addParticipantToMeeting(meeting, participantRequested);
        return new ResponseEntity<Collection<Participant>>(meeting.getParticipants(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/participants/{login}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteParticipantFromMeeting(@PathVariable("id") long id, @PathVariable("login") String login) {
        Meeting meeting = meetingService.findById(id);
        Participant participant = participantService.findByLogin(login);
        if (meeting == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
        if (participant == null) {
            return new ResponseEntity<String>("Unable to remove. A participant with login " + login + " does not exist.",
                    HttpStatus.NOT_FOUND);
        }
        if (!meeting.getParticipants().contains(participant)) {
            return new ResponseEntity<String>("Unable to remove. " +
                    "A participant with login " + login + " has not been added to meeting " + id,
                    HttpStatus.CONFLICT);
        }
        meetingService.removeParticipantFromMeeting(meeting, participant);
        return new ResponseEntity<Collection<Participant>>(meeting.getParticipants(), HttpStatus.OK);
    }

}
