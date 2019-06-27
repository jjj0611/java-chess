package chess.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import chess.database.DatabaseConnection;
import chess.domain.ChessPieceInfo;
import chess.domain.Player;
import chess.domain.Position;
import chess.domain.piece.Piece;
import chess.domain.piece.Type;

public class PieceDAO {
    private static final String INSERT_PIECE =
            "insert into piece(player, piece_type, x_position, y_position, room_number) values(?,?,?,?,?)";
    private static final String DELETE_ALL_PIECES_QUERY =
            "delete from piece where room_number = ?";
    private static final String SELECT_PIECES_QUERY =
            "select player, piece_type, x_position, y_position from piece where room_number = ?";

    private static PieceDAO pieceDAO;

    private PieceDAO() {
    }

    public static PieceDAO getInstance() {
        if (pieceDAO == null) {
            pieceDAO = new PieceDAO();
        }
        return pieceDAO;
    }

    public void addAllPieces(int roomNumber, List<Piece> pieces) throws SQLException {
        for (Piece piece : pieces) {
            addPiece(roomNumber, piece);
        }
    }

    private void addPiece(int roomNumber, Piece piece) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(INSERT_PIECE)) {
            pstmt.setString(1, piece.getPlayerName());
            pstmt.setString(2, piece.getPieceType());
            pstmt.setInt(3, piece.getCoordinateX());
            pstmt.setInt(4, piece.getCoordinateY());
            pstmt.setInt(5, roomNumber);
            pstmt.executeUpdate();
        }
    }

    public void deleteAllPieces(int roomNumber) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(DELETE_ALL_PIECES_QUERY)) {
            pstmt.setInt(1, roomNumber);
            pstmt.executeUpdate();
        }
    }

    public List<Piece> getChessPieces(int roomNumber) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SELECT_PIECES_QUERY)) {
            pstmt.setInt(1, roomNumber);
            return getChessPiece(pstmt);
        }
    }

    private List<Piece> getChessPiece(PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<Piece> pieces = new ArrayList<>();

            while (rs.next()) {
                Player player = Player.valueOf(rs.getString(1));
                Type type = Type.valueOf(rs.getString(2));
                Position position = Position.getPosition(rs.getInt(3), rs.getInt(4));
                pieces.add(ChessPieceInfo.generatePiece(player, type, position));
            }
            return pieces;
        }
    }
}
