package com.finetune.app.model;

import java.util.ArrayList;
import java.util.List;

public class SkiBrand {

	private Long id;

	private String name;

	private List<SkiModel> models = new ArrayList<>();

	public SkiBrand() {}

	public SkiBrand(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SkiModel> getModels() {
		return models;
	}

	public void setModels(List<SkiModel> models) {
		this.models = models;
	}

	public void addModel(SkiModel model) {
		models.add(model);
		model.setBrand(this);
	}
}

