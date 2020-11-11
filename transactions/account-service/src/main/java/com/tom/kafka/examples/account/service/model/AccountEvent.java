package com.tom.kafka.examples.account.service.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import lombok.Builder.Default;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountEvent {
	
	public enum Status {
		NEW("new"),
		REJECTED("rejected"),
		FULFILLED("fulfilled");
		
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

	private Status status = Status.NEW;

	private Type type;
}
