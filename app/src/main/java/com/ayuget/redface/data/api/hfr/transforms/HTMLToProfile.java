package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.data.api.model.Smiley;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToProfile implements Func1<String, Profile> {
    private static final String AVATAR_CELL_START = "<td class=\"profilCase4\" rowspan=\"6\" style=\"text-align:center\">";
    private static final String AVATAR_CELL_END = "</td>";
    private static final String AVATAR_IMAGE_LINK_START = "<img src=\"";
    private static final String AVATAR_IMAGE_LINK_END = "\"";

    private static final String INFO_CELL_LABEL_START = "<td class=\"profilCase2\">";
    private static final String INFO_CELL_CONTENT_START = "<td class=\"profilCase3\">";
    private static final String INFO_CELL_END = "</td>";

    private static final String SIGNATURE_EXTRA_TAGS = "<br /><div style=\"clear: both;\"> </div>";
    private static final String CUSTOM_SMILIES_START = "<td class=\"profilCase4\" rowspan=\"6\">";
    private static final String CUSTOM_SMILIES_SEPARATOR = "<br />";
    private static final String CUSTOM_SMILIES_END = "</td>";

    private static final Pattern CUSTOM_SMILIES_PATTERN = Pattern.compile("((?:\\[)(?:.*?)(?:])).*?(?:<img src=\")(.*?)(?:\")(?:.*?)(?:/>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public Profile call(String s) {
        int currentOffset = 0;

        Cell usernameCell = findNextCell(s, currentOffset);
        if (usernameCell == null) {
            return null;
        }

        String avatarUrl = findAvatarUrl(s);

        Profile.Builder profileBuilder = Profile.builder()
                .username(usernameCell.content)
                .avatarUrl(avatarUrl)
                .status("")
                .arrivalDate("")
                .messageCount(0)
                .personalSmilies(new ArrayList<>());

        Cell emailCell = findNextCell(s, usernameCell.endOffset);
        if (emailCell == null) {
            return profileBuilder.build();
        }

        profileBuilder.emailAddress(emailCell.content);

        Cell birthdayCell = findNextCell(s, emailCell.endOffset);
        if (birthdayCell == null) {
            return profileBuilder.build();
        }

        if (birthdayCell.content.length() > 0) {
            profileBuilder.birthday(birthdayCell.content);
        }

        Cell sexGenreCell = findNextCell(s, birthdayCell.endOffset);
        if (sexGenreCell == null) {
            return profileBuilder.build();
        }

        if (sexGenreCell.content.length() > 0) {
            profileBuilder.sexGenre(sexGenreCell.content);
        }

        Cell cityCell = findNextCell(s, sexGenreCell.endOffset);
        if (cityCell == null) {
            return profileBuilder.build();
        }

        if (cityCell.content.length() > 0) {
            profileBuilder.city(cityCell.content);
        }

        Cell employmentCell = findNextCell(s, cityCell.endOffset);
        if (employmentCell == null) {
            return profileBuilder.build();
        }

        if (employmentCell.content.length() > 0) {
            profileBuilder.employment(employmentCell.content);
        }

        Cell hobbiesCell = findNextCell(s, employmentCell.endOffset);
        if (hobbiesCell == null) {
            return profileBuilder.build();
        }

        if (hobbiesCell.content.length() > 0) {
            profileBuilder.hobbies(hobbiesCell.content);
        }

        Cell statusCell = findNextCell(s, hobbiesCell.endOffset);
        if (statusCell == null) {
            return profileBuilder.build();
        }

        profileBuilder.status(statusCell.content);

        Cell messageCountCell = findNextCell(s, statusCell.endOffset);
        if (messageCountCell == null) {
            return profileBuilder.build();
        }

        profileBuilder.messageCount(Long.valueOf(messageCountCell.content));
        profileBuilder.personalSmilies(parseCustomSmiliesList(s, messageCountCell.endOffset));

        Cell arrivalDateCell = findNextCell(s, messageCountCell.endOffset);
        if (arrivalDateCell == null) {
            return profileBuilder.build();
        }

        profileBuilder.arrivalDate(arrivalDateCell.content);

        Cell lastMessageDateCell = findNextCell(s, arrivalDateCell.endOffset);
        if (lastMessageDateCell == null) {
            return profileBuilder.build();
        }

        if (lastMessageDateCell.content.length() > 0) {
            profileBuilder.lastMessageDate(lastMessageDateCell.content);
        }

        Cell personalQuoteCell = findNextCell(s, lastMessageDateCell.endOffset);
        if (personalQuoteCell == null) {
            return profileBuilder.build();
        }

        if (personalQuoteCell.content.length() > 0) {
            profileBuilder.personalQuote(personalQuoteCell.content);
        }

        Cell signatureCell = findNextCell(s, personalQuoteCell.endOffset);
        if (signatureCell == null) {
            return profileBuilder.build();
        }

        String signatureCellContent = signatureCell.content.replace(SIGNATURE_EXTRA_TAGS, "");
        if (signatureCellContent.length() > 0) {
            profileBuilder.messageSignature(signatureCellContent);
        }

        return profileBuilder.build();
    }

    private Cell findNextCell(String pageContent, int currentOffset) {
        int cellLabelTagIndex = pageContent.indexOf(INFO_CELL_LABEL_START, currentOffset);
        if (cellLabelTagIndex == -1) {
            return null;
        }

        int cellContentStartTagIndex = pageContent.indexOf(INFO_CELL_CONTENT_START, cellLabelTagIndex + INFO_CELL_CONTENT_START.length());
        if (cellContentStartTagIndex == -1) {
            return null;
        }

        int cellContentStartIndex = cellContentStartTagIndex + INFO_CELL_CONTENT_START.length();

        int cellContentEndTagIndex = pageContent.indexOf(INFO_CELL_END, cellContentStartIndex);
        if (cellContentEndTagIndex == -1) {
            return null;
        }

        Cell cell = new Cell();

        cell.content = pageContent.substring(cellContentStartIndex, cellContentEndTagIndex)
                .replaceAll("&nbsp;", " ")
                .trim();

        cell.endOffset = cellContentEndTagIndex + INFO_CELL_END.length();

        return cell;
    }

    private String findAvatarUrl(String pageContent) {
        int avatarCellStartTagIndex = pageContent.indexOf(AVATAR_CELL_START);
        if (avatarCellStartTagIndex == -1) {
            return null;
        }

        int avatarCellContentStartIndex = avatarCellStartTagIndex + AVATAR_CELL_START.length();

        int avatarCellEndTagIndex = pageContent.indexOf(AVATAR_CELL_END, avatarCellContentStartIndex);
        if (avatarCellEndTagIndex == -1) {
            return null;
        }

        String avatarCellContent = pageContent.substring(avatarCellStartTagIndex + AVATAR_CELL_START.length(), avatarCellEndTagIndex).trim();

        if (avatarCellContent.length() == 0) {
            return null; // No avatar
        }

        int avatarLinkStartIndex = pageContent.indexOf(AVATAR_IMAGE_LINK_START, avatarCellContentStartIndex);
        if (avatarLinkStartIndex == -1) {
            return null;
        }

        int avatarLinkContentStart = avatarLinkStartIndex + AVATAR_IMAGE_LINK_START.length();

        int avatarLinkEndIndex = pageContent.indexOf(AVATAR_IMAGE_LINK_END, avatarLinkContentStart);
        if (avatarLinkEndIndex == -1) {
            return null;
        }

        return pageContent.substring(avatarLinkContentStart, avatarLinkEndIndex).trim();
    }

    private List<Smiley> parseCustomSmiliesList(String pageContent, int currentOffset) {
        int customSmiliesStartTagIndex = pageContent.indexOf(CUSTOM_SMILIES_START, currentOffset);
        if (customSmiliesStartTagIndex == -1) {
            return new ArrayList<>();
        }

        int customSmiliesCellContentStartIndex = customSmiliesStartTagIndex + CUSTOM_SMILIES_START.length();

        int customSmiliesCellEndTagIndex = pageContent.indexOf(CUSTOM_SMILIES_END, customSmiliesCellContentStartIndex);
        if (customSmiliesCellEndTagIndex == -1) {
            return new ArrayList<>();
        }

        String customSmiliesCellContent = pageContent.substring(customSmiliesCellContentStartIndex, customSmiliesCellEndTagIndex)
                .replaceAll("&nbsp;", " ")
                .trim();

        Matcher smiliesMatcher = CUSTOM_SMILIES_PATTERN.matcher(customSmiliesCellContent);

        List<Smiley> customSmilies = new ArrayList<>();

        while (smiliesMatcher.find()) {
            customSmilies.add(Smiley.create(smiliesMatcher.group(1), smiliesMatcher.group(2)));
        }

        return customSmilies;
    }

    private static class Cell {
        String content;
        int endOffset;
    }
}
