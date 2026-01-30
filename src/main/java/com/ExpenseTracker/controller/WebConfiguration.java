package com.ExpenseTracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebConfiguration {

	// Match any path that doesn't contain a dot (to avoid catching .js or .css
	// files)
	@RequestMapping(value = "/{path:[^\\.]*}")
	public String redirect() {
		// Forward to home page so React Router can take over
		return "forward:/";
	}
}