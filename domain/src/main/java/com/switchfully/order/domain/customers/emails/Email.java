package com.switchfully.order.domain.customers.emails;

import com.switchfully.order.infrastructure.builder.Builder;

import javax.persistence.Embeddable;

@Embeddable
public final class Email {

    private String localPart;
    private String domain;
    private String complete;

    public Email() {
    }

    private Email(EmailBuilder emailBuilder) {
        this.localPart = emailBuilder.localPart;
        this.domain = emailBuilder.domain;
        this.complete = emailBuilder.complete;
    }

    public void setLocalPart(String localPart) {
        this.localPart = localPart;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setComplete(String complete) {
        this.complete = complete;
    }

    public String getLocalPart() {
        return localPart;
    }

    public String getDomain() {
        return domain;
    }

    public String getComplete() {
        return complete;
    }

    @Override
    public String toString() {
        return "Email{" + "localPart='" + localPart + '\'' +
                ", domain='" + domain + '\'' +
                ", complete='" + complete + '\'' +
                '}';
    }

    public static class EmailBuilder extends Builder<Email> {

        private String localPart;
        private String domain;
        private String complete;

        private EmailBuilder() {
        }

        public static EmailBuilder email() {
            return new EmailBuilder();
        }

        @Override
        public Email build() {
            return new Email(this);
        }

        public EmailBuilder withLocalPart(String localPart) {
            this.localPart = localPart;
            return this;
        }

        public EmailBuilder withDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public EmailBuilder withComplete(String complete) {
            this.complete = complete;
            return this;
        }
    }

}
