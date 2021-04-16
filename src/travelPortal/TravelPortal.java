//Authored by Berk Delibalta

package travelPortal;

import java.util.*;
import static java.util.stream.Collectors.*;
import static  java.util.Comparator.*;

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
            if (!activities.contains(activity))	throw new TPException("The activity is not defined before!");

            agencies.add(new Agency(name, activity));
        }

        return agencies.size();

    }

    public SortedMap<String, List<String>> getAgenciesForActivityTypes() {

        return agencies.stream().sorted(comparing(Agency::getName)).collect(groupingBy(
                Agency::getActivity, TreeMap::new,mapping(Agency::getName,toList())));

    }

    //R2
    public int addProposal(String code, String agency, String destination, String period, int minNP, int maxNP,
                           int price) throws TPException {

        if (proposals.containsKey(code)) throw new TPException("The proposal code has been defined before!");

        if (agencies.stream().noneMatch(e -> e.getName().equals(agency))) throw new TPException("The agency is not existing!");


        proposals.put(code, new Proposal(agency, period, minNP, maxNP, price));

        String theTime = period.trim().split(":")[1];

        int i = Integer.parseInt(theTime.trim().split("-")[0]);
        int f = Integer.parseInt(theTime.trim().split("-")[1]);

        return f - i;
    }

    public int addActivity(String code, String activityType, String description, int price) throws TPException {


        if (agencies.stream().filter(e -> e.getName().equals(proposals.get(code).getAgency()))
                .noneMatch(e -> e.getActivity().equals(activityType))) throw new TPException("The agency does not offer the activity!");


        proposals.get(code).AddActivities(new Activity(activityType, price));

        return proposals.get(code).getActivities().stream().mapToInt(Activity::getPrice).sum();

    }

    public int getProposalPrice(String code) throws TPException {

        if (!proposals.containsKey(code)) throw new TPException("The proposal does not exist!");

        int activitiesTotalPrice = proposals.get(code).getActivities().stream().mapToInt(Activity::getPrice).sum();

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
                        int finish1 = Integer.parseInt((proposal.getPeriod().trim().split(":")[1]).trim().split("-")[1]);

                        int month2 = Integer.parseInt(proposal2.getPeriod().trim().split(":")[0]);
                        int start2 = Integer.parseInt((proposal2.getPeriod().trim().split(":")[1]).trim().split("-")[0]);
                        int finish2 = Integer
                                .parseInt((proposal2.getPeriod().trim().split(":")[1]).trim().split("-")[1]);

                        if (month1 == month2) {

                            if (start2 >= start1 && finish2 >= finish1) throw new TPException("Error: overlapping periods!");

                            continue;
                        }

                    }

                }

                participants.put(new Participant(name), Arrays.asList(code));
                proposal.setAttendees(new Participant(name));

            }

            if (proposal.getAttendees().size() >= proposal.getMaxNP()
                    || proposal.getAttendees().size() < proposal.getMinNP())
                throw new TPException("The number of attendees to the tour is out of range!");

            return proposal.getAttendees().stream().map(Participant::getName).collect(toList());

        }

        return null;
    }

    public int getIncome(String code) {


        int totalActivityPrice = 0;

        if (proposals.get(code).getActivities() != null) {

            for (Activity activity : proposals.get(code).getActivities()) {

                totalActivityPrice += activity.getPrice();

            }

            return (proposals.get(code).getPrice() + totalActivityPrice) * proposals.get(code).getAttendees().size();
        }

        return proposals.get(code).getPrice() * proposals.get(code).getAttendees().size();

    }

    //R4
    public String addRatings(String code, int... evaluations) throws TPException {

        String tmp="";
        int index = 0;

        if (proposals.get(code) != null) {

            if (proposals.get(code).getAttendees().size() != evaluations.length)
                throw new TPException("The number of evaluations should be equal to the number of participants!");

            for (Participant participant : proposals.get(code).getAttendees()) {

                int point = evaluations[index];

                Evaluations.add(new Evaluation(participant.getName(), point));

                if (index != 0)
                    tmp += ", ";
                tmp += participant.getName() + ":" + point;
                ++index;

            }

        }

        return tmp;

    }

    public SortedMap<String, Integer> getTotalRatingsForParticipants() {

        return Evaluations.stream().collect(groupingBy(Evaluation::getParticipant, TreeMap::new,
                mapping(Evaluation::getPoint,summingInt(Integer::intValue))));

    }

//R5

    public SortedMap<String, Integer> incomeForActivityTypes() {

        return proposals.values().stream().flatMap(e -> e.getActivities().stream())
                .sorted(comparing(Activity::getActivityType))
                .collect(groupingBy(Activity::getActivityType, TreeMap::new,
                        mapping(Activity::getPrice,summingInt(Integer::intValue))));

    }

    //lista partecipanti che partecipano allo stesso numero di proposte
    public SortedMap<Integer, List<String>> participantsWithSameNofProposals() {

        return proposals.values().stream().flatMap(e -> e.getAttendees().stream())
                .collect(groupingBy(Participant::getName, TreeMap::new,
                        collectingAndThen(counting(), Long::intValue)))
                .entrySet().stream()
                .collect(groupingBy(x -> x.getValue(),
                        TreeMap::new,
                        mapping(x -> x.getKey(),toList())));

    }
}
