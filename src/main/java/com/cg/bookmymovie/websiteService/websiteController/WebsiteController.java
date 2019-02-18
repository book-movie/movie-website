package com.cg.bookmymovie.websiteService.websiteController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
//import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.cg.bookmymovie.booking.entity.Address;
import com.cg.bookmymovie.booking.entity.Booking;
import com.cg.bookmymovie.movie.movie.entity.Movie;
import com.cg.bookmymovie.screeningservice.entity.Screening;
import com.cg.bookmymovie.screeningservice.entity.Seat;

@RestController
//@EnableOAuth2Sso
public class WebsiteController {

	/*@RequestMapping("/user")
	public Principal user(Principal principal) {
		return principal;
	}*/

	private Integer getUniqueId() {
		UUID idOne = UUID.randomUUID();
		int uid = idOne.hashCode();
		return uid;
	}

	
	private List<Screening> allTheatres = new ArrayList<Screening>();
	private Set<LocalDate> screeningDatesOfAMovie = new HashSet<LocalDate>();
	private String moviePoster = "";
	private Screening screen;				//after selecting theatre it will be available for further processing

	@Autowired
	private RestTemplate template;

	@RequestMapping("/")
	public ModelAndView hello(Model model) {
		
		Set<String> cities = new HashSet<String>();
		
		ResponseEntity<Screening[]> screening = template.getForEntity("http://localhost:9191/screenings",Screening[].class);
		
		List<Screening> allScreenings = Arrays.asList(screening.getBody());
		Set<Screening> moviesScreening = new HashSet<Screening>();
		
		for (Screening screeningToValidate : allScreenings) {
			moviesScreening.add(screeningToValidate);
			String city = screeningToValidate.getTheatreAddress().getCity();
			cities.add(city);
		}	
//added movies screening these days		
		model.addAttribute("screenings", moviesScreening);
		return new ModelAndView("index", "cities",cities);
	}


	@SuppressWarnings("unchecked")
	@RequestMapping("/cityToSearch")
	public ModelAndView searchMovieInACity(@RequestParam String cityToSearch) {

		/*
		 * because after selecting city and getting theatres showing movieif i go back
		 * and selects some different city and then checks theatre showing movie in that
		 * city. i will get theatres of previous city also because i'm storing that data
		 * in member variable as a list.
		 */
		allTheatres.clear();
		screeningDatesOfAMovie.clear();

		Set<Screening> allMovieShowingInACity = new HashSet<Screening>();

		ResponseEntity<Screening[]> screening = template.getForEntity("http://localhost:9191/screenings",
				Screening[].class);

		List<Screening> allScreenings = Arrays.asList(screening.getBody());

		for (Screening screeningToValidate : allScreenings) {

			Address address = screeningToValidate.getTheatreAddress();

			if (address.getCity().equalsIgnoreCase(cityToSearch)) {
				allMovieShowingInACity.add(screeningToValidate);
				allTheatres.add(screeningToValidate);

				 // setting all dates on which this movie will
																			// be screening in this city
			}
		}
		return new ModelAndView("moviesShowing", "movies", allMovieShowingInACity);
	}

	String nameOfTheMovie="";
	@RequestMapping("/getMovieDetails")
	public ModelAndView getMovie(@RequestParam String movieName, Model model) {
		nameOfTheMovie="";
		screeningDatesOfAMovie.clear();
		System.out.println(movieName);
		Movie movie = template.getForObject("http://localhost:9292/movies/" + movieName, Movie.class);
		nameOfTheMovie = movie.getMovieName();
		for (Screening screening : allTheatres) {
			if (screening.getMovieName().equalsIgnoreCase(movieName)) {
				moviePoster = screening.getMoviePoster(); // it is required for subsequent request also
				model.addAttribute("moviePoster", screening.getMoviePoster());
				screeningDatesOfAMovie.add(screening.getDate());

			}
		}
		model.addAttribute("movie", movie);
		return new ModelAndView("movieDetail");
	}

	@RequestMapping("/theatreShowingMovie")
	public ModelAndView showTheatres(Model model) {
		LocalDate todaysdate = LocalDate.now();

		List<Screening> theatreShowingMovieToday = new ArrayList<Screening>();

		for (Screening screening : allTheatres) {
			if (todaysdate.equals(screening.getDate()) && screening.getMovieName().equals(nameOfTheMovie))
				theatreShowingMovieToday.add(screening);
		}

		model.addAttribute("moviePoster", moviePoster);
		model.addAttribute("theatres", theatreShowingMovieToday);
		model.addAttribute("dates", screeningDatesOfAMovie);
		return new ModelAndView("theatres");
	}

	@RequestMapping("/theatreShowingMovieByDate")
	public ModelAndView showTheatresBydate(Model model, @RequestParam String dateAsString) {
		LocalDate date = LocalDate.parse(dateAsString);
		System.out.println(dateAsString);

		List<Screening> theatreShowingMovieOnSpecificDate = new ArrayList<Screening>();
		for (Screening screening : allTheatres) {
			System.out.println(screening.getMovieName() + " and city name: "+screening.getTheatreAddress().getCity()
					+" date is : "+screening.getDate() +"date by user: "+date);
			if (date.equals(screening.getDate()) && screening.getMovieName().equals(nameOfTheMovie)) //to show theatre on a particular date with the movie choosen by user
													
				theatreShowingMovieOnSpecificDate.add(screening);
		}

		model.addAttribute("moviePoster", moviePoster);
		model.addAttribute("theatres", theatreShowingMovieOnSpecificDate); // here theatres is a key
		model.addAttribute("dates", screeningDatesOfAMovie);
		return new ModelAndView("theatres"); // here theatres is a jsp page
	}

	@RequestMapping("/chooseSeats")
	public ModelAndView showSeatsOfTheatre(@RequestParam String theatreName, @RequestParam String theatreState,
			@RequestParam String theatreCity, @RequestParam String theatreArea, @RequestParam String date,
			@RequestParam String startTime, Model model) {

		String[] dateArray = date.split("-");
		int year = Integer.parseInt(dateArray[0]);
		int month = Integer.parseInt(dateArray[1]);
		int day = Integer.parseInt(dateArray[2]);

		String[] timeArray = startTime.split(":");
		Integer hour = Integer.parseInt(timeArray[0]);
		Integer minute = Integer.parseInt(timeArray[1]);

		LocalDate dateIs = LocalDate.of(year, month, day);
		LocalTime timeIs = LocalTime.of(hour, minute);

		System.out.println(dateIs + "  " + timeIs + "  " + allTheatres.size());

		screen = new Screening();
		System.out.println(nameOfTheMovie + " this is movie name");
		for (Screening screening : allTheatres) {
			if (screening.getTheatreName().equals(theatreName) && screening.getMovieName().equals(nameOfTheMovie)
					&& screening.getTheatreAddress().getState().equals(theatreState)
					&& screening.getTheatreAddress().getCity().equals(theatreCity)
					&& screening.getTheatreAddress().getArea().equals(theatreArea) && screening.getDate().equals(dateIs)
					&& screening.getStartTime().equals(timeIs)) {

				System.out.println("yes u get the right theatre with movieName"+screening.getMovieName());
				model.addAttribute("screen", screening);
				screen = screening;
				break;
			}
		}

//mapping of seatType with the numberOfSeat of that type available		
		Map<String, Integer> seatTypes = new HashMap<String, Integer>();

//to get different type of seat available		
		Set<String> typesOfSeat = new HashSet<String>();
		for (Map.Entry<Integer, Seat> entry : screen.getSeat().entrySet()) {

			typesOfSeat.add(entry.getValue().getSeatType());
		}
		Object objArray[] = typesOfSeat.toArray();

//to set number of seat available in different type of seats		

		Map<String, Double> seatsPrice = new HashMap<String, Double>();

		for (int count = 0; count < objArray.length; count++) {
			int seatsAvailable = 0;
			double price = 0.0;

			for (Map.Entry<Integer, Seat> entry : screen.getSeat().entrySet()) {
				if (entry.getValue().getSeatType().equals(objArray[count].toString())
						&& entry.getValue().isAvailable() == true)
					seatsAvailable++;
				price = entry.getValue().getPrice();
			}
			seatTypes.put(objArray[count].toString(), seatsAvailable);
			seatsPrice.put(objArray[count].toString(), price);
		}

		model.addAttribute("seatAvailable", seatTypes);
		model.addAttribute("seatprice", seatsPrice);
		return new ModelAndView("seats");
	}

	
	@RequestMapping("/BookSeat")
	public ModelAndView signIn() {
		return new ModelAndView("signIn");
	}
	
	
	@RequestMapping("/signUp")
	public ModelAndView signUp(@RequestBody Profile profile) {
		return new ModelAndView("signIn");
	}
	
//	@RequestMapping("/BookSeat")
	public ModelAndView bookTicket(String[] seat, Model model) {

		Integer seats[] = new Integer[seat.length];
		for (int count = 0; count < seat.length; count++) {
			seats[count] = Integer.parseInt(seat[count]);
		}

		Map<String, Map<Integer, Character>> seatTypeWithDeatial = new HashMap<String, Map<Integer, Character>>();
		Map<Integer, Character> seatDetails = new HashMap<>();

		Double price = 0.0;
		Map<Integer, Character> detailsOfSeat = new HashMap<>();
		for (int count = 0; count < seats.length; count++) {
			for (Map.Entry<Integer, Seat> entry : screen.getSeat().entrySet()) {

				if (entry.getKey() == seats[count]) {
					detailsOfSeat.clear();
					Integer seatNumber = seats[count];
					Character row = entry.getValue().getSeatRow();
					detailsOfSeat.put(seatNumber, row);
					seatDetails.putAll(detailsOfSeat);

					String seatType = entry.getValue().getSeatType();
					seatTypeWithDeatial.put(seatType, seatDetails);

					price = price + entry.getValue().getPrice();
				}
			}
		}

		Integer userId = 102;
		Booking booking = new Booking(getUniqueId(), userId, screen.getMovieName(), screen.getTheatreName(),
				screen.getTheatreAddress(), screen.getDate(), screen.getStartTime(), seatTypeWithDeatial, price);
//HERE i have to check first use is logged in or not
		ResponseEntity status = template.postForEntity("http://localhost:9393/bookings/", booking, ResponseEntity.class);

//after payment 
		/*if(status.getStatusCode().equals(HttpStatus.OK))
		{
			Map<Integer, Seat> seatToUpdate = screen.getSeat();
			for (int count = 0; count < seats.length; count++) {
				seatToUpdate.get(seats[count]).setAvailable(false);
			}
			 template.put("http://localhost:9191/screenings/", screen);
			ResponseEntity<Screening> updatedScreeningEntity =  template.getForEntity("http://localhost:9191/screenings/"+screen.getId(), Screening.class);
			screen = updatedScreeningEntity.getBody();
			
		}*/
		
		model.addAttribute("seatSelected",seatTypeWithDeatial);
		model.addAttribute("totalPrice", price);
		return new ModelAndView("seatsSummary");
		
	}
}
