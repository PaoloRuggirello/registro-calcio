package com.elis.registrocalcio.handler;

import com.elis.registrocalcio.controller.EventController;
import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.enumPackage.Team;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.UserEvent;
import com.elis.registrocalcio.other.EmailServiceImpl;
import com.elis.registrocalcio.other.DateUtils;
import com.elis.registrocalcio.repository.general.EventRepository;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.repository.general.UserEventRepository;
import com.elis.registrocalcio.repository.general.UserRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.Optional.ofNullable;


@Service
public class EventHandler {
    @Autowired
    EventRepository eventRepository;
    @Autowired
    UserEventRepository userEventRepository;
    @Autowired
    EmailServiceImpl emailService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EventHandler eventHandler;

    private static Logger log = LogManager.getLogger(EventController.class);

    public boolean areFieldsValid(EventDTO event){
        return validateEventCategory(event.getCategory()) && validateDate(DateUtils.StringToInstantConverter(event.getDate()));
    }

    public Event findEventByIdCheckOptional(Long id){
        Optional<Event> eventOptional = eventRepository.findById(id);
        if(eventOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FootballRegisterException.EVENT_NOT_FOUND.toString());
        return eventOptional.get();
    }

    private boolean validateEventCategory(String category){
        return !StringUtils.isBlank(category) && !ObjectUtils.isEmpty(Category.getCategoryFromString(category));
    }

    private boolean validateDate(Instant date){
//        Instant endOfToday = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS); // Can't create event in the givenDay, admin should do that almost 1 DAY before the event
        return !ObjectUtils.isEmpty(date) && Instant.now().isBefore(date);
    }

//    private boolean isAloneInDay(EventDTO event) {
//        boolean isEventAlone = true;
//        Instant date = DateUtils.StringToInstantConverter(event.getDate());
//        Instant startDay = date.truncatedTo(ChronoUnit.DAYS);
//        Instant nextDay = startDay.plus(1l, ChronoUnit.DAYS);
//        List<Event> eventsInDay = eventRepository.findEventInSameDateByDay(startDay, nextDay);
//        for (Event eventInDB : eventsInDay)
//            if(eventInDB.getCategory().equals(Category.getCategoryFromString(event.getCategory()))){
//                isEventAlone = false;
//                break;
//            }
//        return isEventAlone;
//    }

    public void delete(Event event){
        eventRepository.delete(event);
    }

    public List<Event> findAll(){
        return eventRepository.findAll();
    }

    public List<Event> findActiveEvents(String username){
        List<Long> subscribedEvents = userEventRepository.findEventsSubscribedByUser(username, Instant.now()).stream().map(Event::getId).collect(Collectors.toList());
        if(subscribedEvents.size() == 0) return eventRepository.findAllByPlayedIsFalseOrderByDateAsc(Instant.now());
        return eventRepository.findByIdNotIn(subscribedEvents, Instant.now());
    }
    public List<Event> findPastEvents(){
        return eventRepository.findAllByPlayedIsTrue(Instant.now());
    }

    public List<UserEvent> findEventPlayers(Long eventId, Integer page){
        int maxPlayers = eventHandler.findEventByIdCheckOptional(eventId).getCategory().numberOfAllowedPlayers();
        return userEventRepository.findPlayersOfEvent(eventId, PageRequest.of(page, maxPlayers));
    }

    public boolean isTeamsSizeValid(int team1, int team2){
        int major = Math.max(team1, team2);
        int minor = Math.min(team1, team2);
        if( major <= minor + 1) return true; //Teams can have at least one player of difference
        return false;
    }

    public Event save(Event event){
        return eventRepository.save(event);
    }

    /**
     * This method send an email to each user that want to now the creation of a new Event
     * @param event
     */
    public void newEventToNewsLetter(Event event){
        List<String> mailList = userRepository.findNewsLetter();
        if(mailList.size() > 0)
            emailService.comunicateNewEventToMailList(mailList, event.getCategory().toString(), event.getDate());
    }

    public String exportEvent(Event event, String filename) throws IOException, DocumentException {
        String file = "export/" + filename;
        Document exportedMatch = new com.itextpdf.text.Document();
        PdfWriter.getInstance(exportedMatch, new FileOutputStream(file));

        List<String> blackTeam = ofNullable(event.getPlayers()).orElse(new ArrayList<>()).stream().filter(p -> Team.BLACK.equals(p.getTeam())).map(player -> player.getUser().getNameAndSurname()).collect(Collectors.toList());
        List<String> whiteTeam = ofNullable(event.getPlayers()).orElse(new ArrayList<>()).stream().filter(p -> Team.WHITE.equals(p.getTeam())).map(player -> player.getUser().getNameAndSurname()).collect(Collectors.toList());


        exportedMatch.open();
        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD, BaseColor.BLACK);
        Font subTitleFont = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.ITALIC, BaseColor.BLACK);
        Font teamTitleFont = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD, BaseColor.BLACK);
        Font playerFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.ITALIC, BaseColor.BLACK);

        Paragraph title = new Paragraph();
        Paragraph subTitle = new Paragraph();
        Paragraph blackTeamTitle = new Paragraph();
        Paragraph blackTeamPlayers = new Paragraph();
        Paragraph whiteTeamTitle = new Paragraph();
        Paragraph whiteTeamPlayers = new Paragraph();

        title.setAlignment(Element.ALIGN_CENTER);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        blackTeamTitle.setAlignment(Element.ALIGN_BASELINE);
        whiteTeamTitle.setAlignment(Element.ALIGN_BASELINE);
        blackTeamTitle.setSpacingBefore(20);


        Chunk titleText = new Chunk(event.getCategory().toString(), titleFont);
        Chunk subTitleText = new Chunk(DateUtils.getDateFromInstant(event.getDate()) + " " + DateUtils.getHourFromInstant(event.getDate()), subTitleFont);
        Chunk blackTeamTitleText = new Chunk("BLACK:",teamTitleFont);
        Chunk whiteTeamTitleText = new Chunk("WHITE:", teamTitleFont);

        if(whiteTeam.size() > 0 && blackTeam.size() > 0) {
            blackTeam.forEach(player -> blackTeamPlayers.add(new Chunk(player + "\n", playerFont)));
            whiteTeam.forEach(player -> whiteTeamPlayers.add(new Chunk(player + "\n", playerFont)));
        } else {
            blackTeamTitleText = new Chunk("Nessun giocatore presente");
            whiteTeamTitleText = null;
        }


        title.add(titleText);
        subTitle.add(subTitleText);
        blackTeamTitle.add(blackTeamTitleText);
        whiteTeamTitle.add(whiteTeamTitleText);


        exportedMatch.add(title);
        exportedMatch.add(subTitle);
        exportedMatch.add(blackTeamTitle);
        exportedMatch.add(blackTeamPlayers);
        exportedMatch.add(whiteTeamTitle);
        exportedMatch.add(whiteTeamPlayers);
        exportedMatch.close();


        return file;
    }


    public String generateFileName(Event event){
        return event.getCategory() + " - " + DateUtils.getSmallDateFromInstant(event.getDate()) + " " + DateUtils.getHourFromInstant(event.getDate()) +".pdf";
    }

    @Scheduled(fixedRate = 3600000)
    private void deleteFiles() throws IOException {
        File directory = new File("export/");
        FileUtils.cleanDirectory(directory);
        log.warn("Scheduled operation - deleted all files");
    }
}
