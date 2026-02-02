package com.finetune.app.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ski_brands")
public class SkiBrand {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	// Read-only exposure; avoid recursion in JSON
	@OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
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

