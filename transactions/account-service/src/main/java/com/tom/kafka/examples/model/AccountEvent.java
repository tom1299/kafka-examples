package com.tom.kafka.examples.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEvent {
	
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
		DEPOSIT("deposit"),
		WITHDRAW("withdraw"),;

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

	private String userId;
	
	private long amount;
	
	private String accountNumber;

	@Builder.Default
	private Status status = Status.NEW;

	private Type type;
}
