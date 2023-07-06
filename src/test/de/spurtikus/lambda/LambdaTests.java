package de.spurtikus.lambda;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LambdaTests {
    Album album1, album2;

    @Data
    @AllArgsConstructor
    private static class Song {
        private String title;
    }

    @Data
    private static class Album {
        private String name;
        private List<Song> songs;

        public Album(String name) {
            this.name = name;
            songs = new ArrayList<>();
        }
    }

    @Before
    public void before() {
        album1 = new Album("Best Of Rock&Pop");

        Song s = new Song("Strawberry fields forever");
        album1.getSongs().add(s);
        s = new Song("Puttin' on the Ritz");
        album1.getSongs().add(s);
        s = new Song("My heart belongs to daddy");
        album1.getSongs().add(s);
        s = new Song("Daddy was a rolling stone");
        album1.getSongs().add(s);

        album2 = new Album("Average songs from Rock&Pop");

        s = new Song("TNT");
        album2.getSongs().add(s);
        s = new Song("Satisfaction");
        album2.getSongs().add(s);
        s = new Song("Aber bitte mit Sahne");
        album2.getSongs().add(s);
        s = new Song("Griechischer Wein");
        album2.getSongs().add(s);
    }

    @Test
    public void testList_NoLambda() {
        for (int i = 0; i< album1.getSongs().size(); i++) {
            System.out.println(album1.getSongs().get(i).getTitle());
        }
    }

    @Test
    public void testList() {
        album1.getSongs().forEach(s -> System.out.println(s.getTitle()));
    }

    @Test
    public void testFilterAndMap() {
        // This gives Song objects to println() which is not what we want
        album1.getSongs().stream().filter(s -> s.getTitle().contains("daddy")).forEach(System.out::println);
        // use map to transform from song to String
        album1.getSongs().stream().map(s -> s.getTitle()).filter(s -> s.contains("daddy")).forEach(System.out::println);
    }

    @Test
    public void testFlatMap() {
        List<List<Album>> listList = new ArrayList<>();
        List<Album> aList = new ArrayList<>();
        aList.add(album1);
        listList.add(aList);

        List<Album> bList = new ArrayList<>();
        bList.add(album2);
        listList.add(bList);

        // Writes "outer level" of list, i.e. the albums
        listList.forEach(l -> l.forEach(a -> System.out.println(a.getName())));
        // Same but with flatMap; we get rid of the encapsulated Lists earlier
        listList.stream()
                .flatMap(List::stream)
                .map(a->a.getName())
                .forEach(System.out::println);

        // Flatten the listList into a flat List
        List<Album> flat = listList.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        flat.forEach(a -> System.out.println(a.getName()));

        System.out.println("1-------");

        // How to flatten the songs from the Album listList?
        //
        // First we flatten the outer list with "listList.flatMap"
        // Then we have a flattened stream of Albums
        // Then we map: transform from album to song attribute
        // Then we flatten the inner list with second  ...flatMap
        // Then we have a flattened stream of songs
        // This is finally collected into a List (collect(toList())
        List<Song> flatSongs = listList.stream()
                .flatMap(List::stream)
                .map(a -> a.getSongs())
                .flatMap(List::stream)
                .collect(Collectors.toList());
        flatSongs.forEach(a -> System.out.println(a.getTitle()));

        System.out.println("2-------");

        // It is possible to do the map step as part of flatMap call.
        // This is why the word "map" is contained in method name "flatmap"
        // It is both, flatten and mapping, and the mapping function
        // is the parameter of the method (like in map() )
        flatSongs = listList.stream()
                .flatMap(List::stream)
                .flatMap(a -> a.getSongs().stream())
                .collect(Collectors.toList());
        flatSongs.forEach(a -> System.out.println(a.getTitle()));
    }
}
