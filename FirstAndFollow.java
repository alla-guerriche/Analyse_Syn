package Analyse_Syn;

import java.util.*;

public class FirstAndFollow {
    public static final String EPSILON = "$";

    private static boolean isSubset(Set<String> setA, Set<String> setB) {
        if (setA == null || setB == null) {
            return false;
        }
        boolean aContainsEpsilon = false;
        boolean bContainsEpsilon = false;
        boolean returnValue;
        if (!setA.isEmpty() && setA.contains(EPSILON)) {
            aContainsEpsilon = true;
        } else {
            setA.add(EPSILON);
        }
        if (!setB.isEmpty() && setB.contains(EPSILON)) {
            bContainsEpsilon = true;
        } else {
            setB.add(EPSILON);
        }
        if (setB.containsAll(setA)) {
            returnValue = true;
        } else
            returnValue = false;
        if (!aContainsEpsilon) {
            setA.remove(EPSILON);
        }
        if (!bContainsEpsilon) {
            setB.remove(EPSILON);
        }
        return returnValue;
    }

    private static String[] parseProduction(String production) {
        return production.split("-");
    }

    public static Map<String, Set<String>> computeFirst(Grammar g) {
        Map<String, Set<String>> first = new LinkedHashMap<>();
        for (String t : g.getTerminals()) {
            first.put(t, new HashSet<>(Collections.singletonList(t)));
        }
        for (String v : g.getVariables()) {
            first.put(v, new HashSet<>(Collections.emptyList()));
        }

        boolean change = true;
        String[] productionArray;

        while (change) {
            change = false;
            for (Map.Entry<String, ArrayList<String>> entry : g.getRules().entrySet()) {
                for (String production : entry.getValue()) {
                    productionArray = parseProduction(production);

                    if (first.get(productionArray[0]).contains(EPSILON)) {
                        if (!first.get(entry.getKey()).contains(EPSILON)) {
                            first.get(entry.getKey()).add(EPSILON);
                            change = true;
                        }
                    }
                    // To handle rules containing left Recursion
                    if (productionArray[0].equals(entry.getKey())) {
                        for (int i = 0; i < productionArray.length - 1; i++) {
                            if (first.get(productionArray[i]).contains(EPSILON)) {
                                first.get(entry.getKey()).addAll(first.get(productionArray[i + 1]));
                            }
                        }
                    }

                    if (!first.get(productionArray[0]).isEmpty()
                            && !isSubset(first.get(productionArray[0]), first.get(entry.getKey()))) {
                        first.get(entry.getKey()).addAll(first.get(productionArray[0]));
                        change = true;
                    }
                }
            }
        }
        return first;
    }

    public static Map<String, Set<String>> computeFollow(Grammar g) {
        Map<String, Set<String>> first = new LinkedHashMap<>(computeFirst(g));
        Map<String, Set<String>> follow = new LinkedHashMap<>();
        for (String v : g.getVariables()) {
            if (v.equals(g.getStartingVariable())) {
                follow.put(v, new HashSet<>(Collections.singleton(EPSILON)));
            } else
                follow.put(v, new HashSet<>(Collections.emptyList()));
        }

        for (String t : g.getTerminals()) {
            follow.put(t, new HashSet<>(Collections.emptyList()));
        }
        boolean change = true;
        while (change) {
            change = false;
            String[] productionArray;
            for (Map.Entry<String, ArrayList<String>> entry : g.getRules().entrySet()) {
                for (String production : entry.getValue()) {
                    productionArray = parseProduction(production);
                    if (productionArray.length >= 3) {
                        for (int i = productionArray.length; i > 1; i--) {
                            if (!isSubset(first.get(productionArray[productionArray.length - i + 1]),
                                    follow.get(productionArray[productionArray.length - i]))) {
                                follow.get(productionArray[productionArray.length - i])
                                        .addAll(first.get(productionArray[productionArray.length - i + 1]));
                                change = true;
                            }
                        }

                        if (g.getVariables().contains(productionArray[productionArray.length - 2])) {
                            if (first.get(productionArray[productionArray.length - 1]).contains(EPSILON)) {
                                follow.get(productionArray[productionArray.length - 2])
                                        .addAll(follow.get(productionArray[productionArray.length - 1]));
                            }
                        }
                    } else {
                        if (productionArray.length >= 2) {
                            for (int i = productionArray.length; i > 1; i--) {
                                if (first.get(productionArray[productionArray.length - i + 1]).contains(EPSILON)) {
                                    if (!isSubset(follow.get(entry.getKey()),
                                            follow.get(productionArray[productionArray.length - i]))) {
                                        follow.get(productionArray[productionArray.length - i])
                                                .addAll(follow.get(entry.getKey()));
                                        change = true;
                                    }
                                }
                            }
                        }
                    }

                    if (g.getVariables().contains(productionArray[productionArray.length - 1])) {
                        follow.get(productionArray[productionArray.length - 1]).addAll(follow.get(entry.getKey()));
                        follow.get(entry.getKey()).addAll(follow.get(productionArray[productionArray.length - 1]));
                    }
                }
            }
        }
        return follow;
    }

    public static void toStringFirst(Grammar g, Map<String, Set<String>> first) {
        System.out.println("First:");
        for (String v : g.getVariables()) {
            System.out.println(v + " = " + first.get(v));
        }
        System.out.println();
    }

    public static void toStringFollow(Grammar g, Map<String, Set<String>> follow) {
        System.out.println("Follow:");
        for (String v : g.getVariables()) {
            System.out.println(v + " = " + follow.get(v));
        }
        System.out.println();
    }

    public static void main(String[] args) {
      

        /*
         * Grammar2
         * E -> TE'
         * E' -> +TE' | $
         * T -> FT'
         * T' -> *FT'|$
         * F -> (E)|id
         */
        String startingVariable2 = "E";
        Set<String> v2 = new HashSet<>();
        v2.add("E");
        v2.add("E'");
        v2.add("T");
        v2.add("T'");
        v2.add("F");
        Set<String> t2 = new HashSet<>();
        t2.add("+");
        t2.add("*");
        t2.add(")");
        t2.add(" (");
        t2.add("id");
        t2.add(EPSILON);
        Map<String, ArrayList<String>> rules2 = new HashMap<>();
        ArrayList<String> list21 = new ArrayList<>();
        list21.add("T-E'");
        rules2.put("E", list21);
        ArrayList<String> list22 = new ArrayList<>();
        list22.add("+-T-E'");
        list22.add(EPSILON);
        rules2.put("E'", list22);
        ArrayList<String> list23 = new ArrayList<>();
        list23.add("F-T'");
        rules2.put("T", list23);
        ArrayList<String> list24 = new ArrayList<>();
        list24.add("*-F-T'");
        list24.add(EPSILON);
        rules2.put("T'", list24);
        ArrayList<String> list25 = new ArrayList<>();
        list25.add("(-E-)");
        list25.add("id");
        rules2.put("F", list25);
        Grammar g2 = new Grammar(startingVariable2, v2, t2, rules2);
        toStringFirst(g2, computeFirst(g2));
        toStringFollow(g2, computeFollow(g2));

       
    }

}