package com.qwict.isbin.controller;

import com.qwict.isbin.dto.BookDto;
import com.qwict.isbin.model.Book;
import com.qwict.isbin.service.BookService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @RequestMapping(value = "/book/{id}")
    public String getBookById(@PathVariable("id") String id, Model model) {
        model.addAttribute("activePage", "book");
        // throw a 404 if the book doesn't exist

        try {
            Long.parseLong(id);
        } catch (NumberFormatException e) {
            // TODO: make a BAD_REQUEST response page
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The id must be a number.");
        }
        if (id == null || id.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        Book bookFromDatabase;
        bookFromDatabase = bookService.getById(id);
        if (bookFromDatabase == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        BookDto bookDto = bookService.mapToBookDto(bookFromDatabase);
        String coverURL = "/images/bookPlaceholder.jpg";

        model.addAttribute("book", bookDto);
        JSONObject response = bookService.getBookFromRemoteAPI(bookFromDatabase.getIsbn());

        JSONObject remoteBook = (JSONObject) response.get(String.format("ISBN:%s", bookFromDatabase.getIsbn()));
        if (remoteBook != null) {
            model.addAttribute("hasRemoteDetails", true);

            JSONObject details = (JSONObject) remoteBook.get("details");
            JSONArray covers = (JSONArray) details.get("covers");
            if (covers != null) {
                List<String> coverIds = new ArrayList<>();
                for (Object cover : covers) {
                    coverIds.add(cover.toString());
                }
                coverURL = String.format("https://covers.openlibrary.org/b/id/%s-L.jpg", coverIds.get(0));
            }

            String remotePublishDate = (String) details.get("publish_date");
            model.addAttribute("remotePublishDate", remotePublishDate);

            String remoteDescription = (String) details.get("description");
            model.addAttribute("remoteDescription", remoteDescription);
        } else {
            model.addAttribute("hasRemoteDetails", false);
        }

        model.addAttribute("coverURL", coverURL);
        return "public/book";
    }

    @RequestMapping(value = "/book/most-popular")
    public String index(Model model) {
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", false);
        model.addAttribute("activePage", "book");

        model.addAttribute("title", "ISBIN Most Popular");
        model.addAttribute("message", "Welcome to the ISBIN Most Popular page!");

        List<BookDto> bookDtos = bookService.getMostPopularBookDtos();

        model.addAttribute("bookDtos", bookDtos);
        return "public/most-popular";
    }


}
