package com.switchfully.order.domain.customers.phonenumbers;

import com.switchfully.order.infrastructure.builder.Builder;

import javax.persistence.Embeddable;

@Embeddable
public final class PhoneNumber {

    private String number;
    private String countryCallingCode;

    public PhoneNumber() {
    }

    private PhoneNumber(PhoneNumberBuilder phoneNumberBuilder) {
        this.number = phoneNumberBuilder.number;
        this.countryCallingCode = phoneNumberBuilder.countryCallingCode;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setCountryCallingCode(String countryCallingCode) {
        this.countryCallingCode = countryCallingCode;
    }

    public String getNumber() {
        return number;
    }

    public String getCountryCallingCode() {
        return countryCallingCode;
    }

    @Override
    public String toString() {
        return "PhoneNumber{" + "number='" + number + '\'' +
                ", countryCallingCode='" + countryCallingCode + '\'' +
                '}';
    }

    public static class PhoneNumberBuilder extends Builder<PhoneNumber> {
        private String number;
        private String countryCallingCode;

        private PhoneNumberBuilder() {
        }

        public static PhoneNumberBuilder phoneNumber() {
            return new PhoneNumberBuilder();
        }

        @Override
        public PhoneNumber build() {
            return new PhoneNumber(this);
        }

        public PhoneNumberBuilder withNumber(String number) {
            this.number = number;
            return this;
        }

        public PhoneNumberBuilder withCountryCallingCode(String countryCallingCode) {
            this.countryCallingCode = countryCallingCode;
            return this;
        }
    }

}
