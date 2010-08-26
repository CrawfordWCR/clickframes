package org.clickframes.testframes.selenium;

import org.clickframes.util.ClickframeUtils;

public class SeleniumTestStep {
    private String command;
    private String target;
    private String value;

    public SeleniumTestStep(String command, String target, String value) {
        this.command = command;
        this.target = target;
        this.value = value;
    }

    public String getCommand() {
        return command;
    }

    @Deprecated
    public String getEscapedCommand() {
        return getCommandEscaped();
    }

    public String getCommandEscaped() {
        return ClickframeUtils.escapeXml(command);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetEscaped() {
        return ClickframeUtils.escapeXml(target);
    }

    @Deprecated
    public String getEscapedTarget() {
        return getTargetEscaped();
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getValue() {
        return value;
    }

    @Deprecated
    public String getEscapedValue() {
        return getValueEscaped();
    }

    public String getValueEscaped() {
        return ClickframeUtils.escapeXml(value);
    }

    public void setValue(String value) {
        this.value = value;
    }
}