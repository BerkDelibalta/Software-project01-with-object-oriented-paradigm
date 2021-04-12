//Authored by Berk Delibalta

package travelPortal;

import java.util.*;
import java.util.stream.Collectors;

public class TravelPortal {

	private List<String> activities;
	private List<Agency> agencies;
	private Map<String, Proposal> proposals;
	private Map<Participant, List<String>> participants;
	private List<Evaluation> Evaluations;

	public TravelPortal() {

		activities = new LinkedList<>();
		agencies = new LinkedList<>();
		proposals = new HashMap<>();
		participants = new HashMap<>();
		Evaluations = new LinkedList<>();

	}

//R1
	public List<String> addActivityTypes(String... names) {
		for (String name : names) {
			activities.add(name);
		}

		Collections.sort(activities);

		return activities;

	}

	public int AddTravelAgency(String name, String... activityTypes) throws TPException {

		if (agencies.stream().anyMatch(e -> e.getName().equals(name)))
			throw new TPException("The agency already defined!");

		for (String activity : activityTypes) {
			if (!activities.contains(activity)) throw new TPException("The activity is not defined before!");

			agencies.add(new Agency(name, activity));
		}

		return agencies.size();

	}

	public SortedMap<String, List<String>> getAgenciesForActivityTypes() {

		return agencies.stream().sorted(Comparator.comparing(Agency::getName)).collect(Collectors.groupingBy(
				Agency::getActivity, TreeMap::new, Collectors.mapping(Agency::getName, Collectors.toList())));

	}

//R2
	public int addProposal(String code, String agency, String destination, String period, int minNP, int maxNP,
			int price) throws TPException {

		if (proposals.containsKey(code)) throw new TPException("The proposal code has been defined before!");

		if (agencies.stream().noneMatch(e -> e.getName().equals(agency))) throw new TPException("The agency is not existing!");

		Proposal proposal = new Proposal(agency, period, minNP, maxNP, price);

		proposals.put(code, proposal);

		String theTime = period.trim().split(":")[1];

		int i = Integer.parseInt(theTime.trim().split("-")[0]);
		int f = Integer.parseInt(theTime.trim().split("-")[1]);

		return f - i;
	}

	public int addActivity(String code, String activityType, String description, int price) throws TPException {

		Proposal proposal = proposals.get(code);

		if (agencies.stream().filter(e -> e.getName().equals(proposal.getAgency()))
				.noneMatch(e -> e.getActivity().equals(activityType))) throw new TPException("The agency does not offer the activity!");

		Activity activity = new Activity(activityType, price);
		proposal.AddActivities(activity);

		return proposals.get(code).getActivities().stream().map(e -> e.getPrice())
				.collect(Collectors.summingInt(e -> e));

	}

	public int getProposalPrice(String code) throws TPException {
	
		if (!proposals.containsKey(code)) throw new TPException("The proposal does not exist!");

		int activitiesTotalPrice = proposals.get(code).getActivities().stream().map(e -> e.getPrice())
				.collect(Collectors.summingInt(e -> e));

		return proposals.get(code).getPrice() + activitiesTotalPrice;

	}

//R3
	public List<String> addParticipants(String code, String... names) throws TPException {

		Proposal proposal = proposals.get(code);

		if (proposal != null) {

			for (String name : names) {

				if (participants.keySet().stream().anyMatch(e -> e.getName().equals(name))) {

					String code2 = participants.entrySet().stream().filter(e -> e.getKey().getName().equals(name))
							.flatMap(e -> e.getValue().stream()).filter(e -> !e.equals(code)).findFirst().get();

					if (!code2.equals(null)) {

						Proposal proposal2 = proposals.get(code2);

						int month1 = Integer.parseInt(proposal.getPeriod().trim().split(":")[0]);
						int start1 = Integer.parseInt((proposal.getPeriod().trim().split(":")[1]).trim().split("-")[0]);
						int finish1 = Integer
								.parseInt((proposal.getPeriod().trim().split(":")[1]).trim().split("-")[1]);

						int month2 = Integer.parseInt(proposal2.getPeriod().trim().split(":")[0]);
						int start2 = Integer
								.parseInt((proposal2.getPeriod().trim().split(":")[1]).trim().split("-")[0]);
						int finish2 = Integer
								.parseInt((proposal2.getPeriod().trim().split(":")[1]).trim().split("-")[1]);

						if (month1 == month2) {

							if (start2 >= start1 && finish2 >= finish1) throw new TPException("Error: overlapping periods!");
							
							continue;
						}

					}

				}

				Participant participant = new Participant(name);
				participants.put(participant, Arrays.asList(code));
				proposal.setAttendees(participant);

			}

			if (proposal.getAttendees().size() >= proposal.getMaxNP()
					|| proposal.getAttendees().size() < proposal.getMinNP()) throw new TPException("The number of attendees to the tour is out of range!");

			return proposal.getAttendees().stream().map(Participant::getName).collect(Collectors.toList());

		}

		return null;
	}

	public int getIncome(String code) {

		List<Activity> activityList = proposals.get(code).getActivities();

		int totalActivityPrice = 0;

		if (activityList != null) {

			for (Activity activity : activityList) {

				totalActivityPrice += activity.getPrice();

			}

			return (proposals.get(code).getPrice() + totalActivityPrice) * proposals.get(code).getAttendees().size();
		}

		return proposals.get(code).getPrice() * proposals.get(code).getAttendees().size();

	}

//R4
	public String addRatings(String code, int... evaluations) throws TPException {

		int evaluationsSize = evaluations.length;
		Proposal proposal = proposals.get(code);
		String tmp = new String();
		tmp = "";
		int index = 0;

         if (proposal != null) {

             if (proposal.getAttendees().size() != evaluationsSize) throw new TPException("The number of evaluations should be equal to the number of participants!");

			for (Participant participant : proposal.getAttendees()) {

				int point = evaluations[index];

				Evaluation evaluation = new Evaluation(participant.getName(), point);

				Evaluations.add(evaluation);

				if (index != 0)
					tmp += ", ";
				tmp += participant.getName() + ":" + point;
				++index;

			}

		}

		return tmp;

	}

	public SortedMap<String, Integer> getTotalRatingsForParticipants() {

		return Evaluations.stream().collect(Collectors.groupingBy(Evaluation::getParticipant, TreeMap::new,
				Collectors.mapping(Evaluation::getPoint, Collectors.summingInt(e -> e))));

	}

//R5

	public SortedMap<String, Integer> incomeForActivityTypes() {

		return proposals.entrySet().stream().flatMap(e -> e.getValue().getActivities().stream())
				.sorted(Comparator.comparing(Activity::getActivityType))
				.collect(Collectors.groupingBy(Activity::getActivityType, TreeMap::new,
						Collectors.mapping(Activity::getPrice, Collectors.summingInt(e -> e))));

	}


	public SortedMap<Integer, List<String>> participantsWithSameNofProposals() {

		return proposals.entrySet().stream().flatMap(e -> e.getValue().getAttendees().stream())
				.collect(Collectors.groupingBy(Participant::getName, TreeMap::new,
						Collectors.collectingAndThen(Collectors.counting(), Long::intValue)))
				.entrySet().stream()
				.collect(Collectors.groupingBy(x -> x.getValue(),
						() -> new TreeMap<Integer, List<String>>(Comparator.naturalOrder()),
						Collectors.mapping(x -> x.getKey(), Collectors.toList())));

	}
}
