package com.tom.kafka.examples.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import lombok.Builder.Default;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockEvent {
	
	public enum Status {
		NEW("new"),
		REJECTED("rejected"),
		FULFILLED("fulfilled"),
		COMPENSATION("compensation");

		@Getter
		@Setter
		@JsonValue
		private String name;
		
		private Status(String name) {
			this.name = name;
		}
	}

	public enum Type {
		ADDITION("addition"),
		REMOVAL("removal"),;

		@Getter
		@Setter
		@JsonValue
		private String name;

		private Type(String name) {
			this.name = name;
		}
	}

	private String id;

	private String transactionId;

	private long amount;
	
	private String stock;

	@Default
	private Status status = Status.NEW;

	private Type type;
}
