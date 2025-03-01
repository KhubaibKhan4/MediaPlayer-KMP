import UIKit
import youtube_ios_player_helper

class YouTubePlayerUIView: UIView, YTPlayerViewDelegate {
    private var playerView: YTPlayerView!

    @objc var videoId: String = "" {
        didSet {
            loadVideo()
        }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupPlayer()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupPlayer()
    }

    private func setupPlayer() {
        playerView = YTPlayerView()
        playerView.delegate = self
        playerView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(playerView)

        NSLayoutConstraint.activate([
            playerView.leadingAnchor.constraint(equalTo: leadingAnchor),
            playerView.trailingAnchor.constraint(equalTo: trailingAnchor),
            playerView.topAnchor.constraint(equalTo: topAnchor),
            playerView.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
    }

    private func loadVideo() {
        if !videoId.isEmpty {
            let playerVars: [String: Any] = [
                "playsinline": 1,
                "autoplay": 0,
                "controls": 1
            ]
            playerView.load(withVideoId: videoId, playerVars: playerVars)
        }
    }
}
